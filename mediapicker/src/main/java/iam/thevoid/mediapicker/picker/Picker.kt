package iam.thevoid.mediapicker.picker

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.annotation.StringRes
import iam.thevoid.ae.asActivity
import iam.thevoid.ae.asFragmentActivity
import iam.thevoid.ae.string
import iam.thevoid.e.safe
import iam.thevoid.mediapicker.R
import iam.thevoid.mediapicker.bus.MediaPickerBus
import iam.thevoid.mediapicker.bus.SelectAppBus
import iam.thevoid.mediapicker.chooser.IntentData
import iam.thevoid.mediapicker.chooser.PickerSelectAppDialog
import iam.thevoid.mediapicker.picker.HiddenPickerFragment.Companion.getFragment
import iam.thevoid.mediapicker.picker.Purpose.Pick
import iam.thevoid.mediapicker.picker.Purpose.Take
import iam.thevoid.mediapicker.picker.metrics.Duration
import iam.thevoid.mediapicker.picker.metrics.MemorySize
import iam.thevoid.mediapicker.picker.metrics.Resolution
import iam.thevoid.mediapicker.picker.metrics.SizeUnit
import iam.thevoid.mediapicker.util.IntentUtils
import java.util.*
import java.util.concurrent.TimeUnit

abstract class Picker<T> protected constructor() {

    private var purposes = listOf<Purpose>()

    private var onDismissListener: OnDismissListener? = null

    @StringRes
    private var chooserTitleResource: Int = -1

    private var chooserTitle: String? = null

    // Bytes
    private var photoMaxSize: Long = 0

    // Bytes
    private var videoMaxSize: Long = 0

    // Pixels
    private var photoMaxPixelsWidth: Long = 0

    // Pixels
    private var photoMaxPixelsHeight: Long = 0

    // Seconds
    private var videoMaxDuration: Long = 0

    private val bundle: Bundle by lazy {
        Bundle().apply {
            putLong(EXTRA_PHOTO_MAX_PIXEL_HEIGHT, photoMaxPixelsHeight)
            putLong(EXTRA_PHOTO_MAX_PIXEL_WIDTH, photoMaxPixelsWidth)
            putLong(EXTRA_PHOTO_MAX_SIZE, photoMaxSize)
            putLong(EXTRA_VIDEO_MAX_DURATION, videoMaxDuration)
            putLong(EXTRA_VIDEO_MAX_SIZE, videoMaxSize)
        }
    }

    abstract fun initStream(): T

    abstract fun requestPermissions(
            context: Context,
            permissions: List<String>,
            result: OnRequestPermissionsResult
    )

    abstract fun onResult(uri: Uri)

    abstract fun onEmptyResult()

    fun request(context: Context): T = initStream().also {
        SelectAppBus.attachMediaPicker(this)
        when {
            onlyOneAppCanHandleRequest(context) ->
                purposes.first()
                        .also { handleIntent(context, it.getIntentData(context, bundle)) }
            else -> {
                chooserTitle.safe(context.string(chooserTitleResource)).also { title ->
                    PickerSelectAppDialog
                            .showForResult(context, intentData(context), title)
                }
            }
        }
    }

    internal fun onAppSelect(context: Context, intentData: IntentData?) =
            intentData?.also { handleIntent(context, it) }

    internal fun onImagePickDismissed() {
        onDismissListener?.onDismiss()
    }

    internal fun onImagePicked(uri: Uri?) = uri?.also(::onResult) ?: onEmptyResult()

    private fun handleIntent(context: Context, intentData: IntentData) {
        requestPermissions(context, intentData.permissions, object : OnRequestPermissionsResult {
            override fun onRequestPermissionsResult(granted: Boolean) {
                if (granted) {
                    startImagePick(context, intentData.intent, intentData.requestCode)
                }
            }

            override fun onRequestPermissionsFailed(throwable: Throwable) {
                Log.d(TAG, "onRequestPermissionsFailed", throwable)
            }
        })
    }

    private fun intentData(context: Context): List<IntentData> =
            purposes.map { it.getIntentData(context, bundle) }

    private fun startImagePick(context: Context, intent: Intent, requestCode: Int) {
        MediaPickerBus.attachMediaPicker(this)
        context.asFragmentActivity().supportFragmentManager
                .beginTransaction()
                .add(getFragment(intent, requestCode), HiddenPickerFragment::class.java.canonicalName)
                .commitAllowingStateLoss()
    }

    private fun onlyOneAppCanHandleRequest(context: Context): Boolean = purposes.map {
        IntentUtils.getResolveInfoList(context.asActivity().packageManager,
                it.getIntent(context, bundle))
    }.flatten().size == 1

    abstract class Builder<T, Picker : iam.thevoid.mediapicker.picker.Picker<T>> {
        private var purposes: Set<Purpose> = setOf()
        private var onDismissListener: OnDismissListener? = null
        private var videoMaxDuration = Duration(15, TimeUnit.SECONDS)
        private var photoMaxResolution = Resolution(3000, 3000)
        private var photoMaxSize = MemorySize(5, SizeUnit.MEGABYTE)
        private var videoMaxSize = MemorySize(10, SizeUnit.MEGABYTE)
        private var appChooserTitleResource = R.string.default_chooser_title
        private var appChooserTitle: String? = null

        protected abstract fun create(): Picker

        fun takeVideo() = apply {
            purposes = setOf(Take.Video())
        }.build()

        fun takePhoto() = apply {
            purposes = setOf(Take.Photo())
        }.build()

        fun pickImage() = apply {
            purposes = setOf(Pick.Image)
        }.build()

        fun pickVideo() = apply {
            purposes = setOf(Pick.Video)
        }.build()

        fun onDismiss(onDismissListener: OnDismissListener) =
                apply { this.onDismissListener = onDismissListener }

        fun setVideoMaxDuration(videoMaxDuration: Duration) =
                apply { this.videoMaxDuration = videoMaxDuration }

        fun setVideoMaxDuration(duration: Long, timeUnit: TimeUnit) =
                setVideoMaxDuration(Duration(duration, timeUnit))

        fun setPhotoMaxSize(photoMaxSize: MemorySize) =
                apply { this.photoMaxSize = photoMaxSize }

        fun setPhotoMaxSize(size: Int, sizeUnit: SizeUnit) =
                setPhotoMaxSize(MemorySize(size, sizeUnit))

        fun setVideoMaxSize(videoMaxSize: MemorySize) =
                apply { this.videoMaxSize = videoMaxSize }

        fun setVideoMaxSize(size: Int, sizeUnit: SizeUnit) =
                setVideoMaxSize(MemorySize(size, sizeUnit))

        fun setPhotoMaxResolution(photoMaxResolution: Resolution) =
                apply { this.photoMaxResolution = photoMaxResolution }

        fun setPhotoMaxResolution(widthPixels: Long, heightPixels: Long) =
                setPhotoMaxResolution(Resolution(widthPixels, heightPixels))

        fun setChooserTitle(@StringRes titleResource: Int) {
            appChooserTitleResource = titleResource
            appChooserTitle = null
        }

        fun setChooserTitle(title: String) {
            appChooserTitleResource = -1
            appChooserTitle = title
        }

        fun build(): Picker =
                create().apply {
                    purposes = ArrayList(this@Builder.purposes)
                    onDismissListener = this@Builder.onDismissListener
                    videoMaxDuration = this@Builder.videoMaxDuration.seconds
                    photoMaxSize = this@Builder.photoMaxSize.bytes
                    videoMaxSize = this@Builder.videoMaxSize.bytes
                    photoMaxPixelsHeight = this@Builder.photoMaxResolution.height
                    photoMaxPixelsWidth = this@Builder.photoMaxResolution.width
                    chooserTitle = appChooserTitle
                    chooserTitleResource = appChooserTitleResource
                }
    }

    interface OnDismissListener {
        fun onDismiss()
    }

    companion object {

        private val TAG = Picker::class.java.simpleName

        const val EXTRA_INTENT = "EXTRA_INTENT"
        const val EXTRA_REQUEST_CODE = "EXTRA_REQUEST_CODE"
        const val EXTRA_PHOTO_MAX_SIZE = "EXTRA_PHOTO_MAX_SIZE"
        const val EXTRA_VIDEO_MAX_SIZE = "EXTRA_VIDEO_MAX_SIZE"
        const val EXTRA_PHOTO_MAX_PIXEL_WIDTH = "EXTRA_PHOTO_MAX_PIXEL_WIDTH"
        const val EXTRA_PHOTO_MAX_PIXEL_HEIGHT = "EXTRA_PHOTO_MAX_PIXEL_HEIGHT"
        const val EXTRA_VIDEO_MAX_DURATION = "EXTRA_VIDEO_MAX_DURATION"
    }
}