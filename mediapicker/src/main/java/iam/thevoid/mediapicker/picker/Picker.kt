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
import iam.thevoid.mediapicker.picker.options.PhotoOptions
import iam.thevoid.mediapicker.picker.options.VideoOptions
import iam.thevoid.mediapicker.util.IntentUtils
import java.util.*
import kotlin.time.ExperimentalTime

abstract class Picker<T> protected constructor() {

    private var purposes = listOf<Purpose>()

    private var onDismissListener: OnDismissListener? = null

    @StringRes
    private var chooserTitleResource: Int = -1

    private var chooserTitle: String? = null

    private var photoOptions: PhotoOptions? = null

    private var videoOptions: VideoOptions? = null

    @OptIn(ExperimentalTime::class)
    private val bundle: Bundle by lazy {
        Bundle().apply {
            photoOptions?.apply {
                maxResolution.height.takeIf { it > 0 }
                        ?.also { putLong(EXTRA_PHOTO_MAX_PIXEL_HEIGHT, it) }

                maxResolution.width.takeIf { it > 0 }
                        ?.also { putLong(EXTRA_PHOTO_MAX_PIXEL_WIDTH, it) }

                maxSize.takeIf { it.bytes > 0 }
                        ?.also { putLong(EXTRA_PHOTO_MAX_SIZE, it.bytes) }
            }

            videoOptions?.apply {
                maxDuration.inSeconds.toLong().takeIf { it > 0 }
                        ?.also { putLong(EXTRA_VIDEO_MAX_DURATION, it) }

                maxSize.takeIf { it.bytes > 0 }
                        ?.also { putLong(EXTRA_VIDEO_MAX_SIZE, it.bytes) }

                putInt(EXTRA_VIDEO_QUALITY, quality.code)
            }
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
        private var photoOptions: PhotoOptions? = null
        private var videoOptions: VideoOptions? = null
        private var appChooserTitleResource = R.string.default_chooser_title
        private var appChooserTitle: String? = null

        protected abstract fun create(): Picker

        @JvmOverloads
        fun takeVideo(options: VideoOptions = VideoOptions()) = apply {
            purposes = setOf(Take.Video(options.chooserTitle))
            videoOptions = options
        }.build()

        @JvmOverloads
        fun pickVideo(options: VideoOptions = VideoOptions()) = apply {
            purposes = setOf(Pick.Video)
            videoOptions = options
        }.build()

        @JvmOverloads
        fun takePhoto(options: PhotoOptions = PhotoOptions()) = apply {
            purposes = setOf(Take.Photo(options.chooserTitle))
            photoOptions = options
        }.build()

        fun pickImage() = apply {
            purposes = setOf(Pick.Image)
        }.build()

        @JvmOverloads
        fun pickImageOrPhoto(photoOptions: PhotoOptions = PhotoOptions()) = apply {
            purposes = setOf(Pick.Image, Take.Photo(photoOptions.chooserTitle))
            this.photoOptions = photoOptions
        }.build()

        fun onDismiss(onDismissListener: OnDismissListener) =
                apply { this.onDismissListener = onDismissListener }

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
                    photoOptions = this@Builder.photoOptions
                    videoOptions = this@Builder.videoOptions
                    chooserTitle = appChooserTitle
                    chooserTitleResource = appChooserTitleResource
                }
    }

    interface OnDismissListener {
        fun onDismiss()
    }

    companion object {

        private val TAG = Picker::class.java.simpleName

        const val EXTRA_INTENT = "iam.thevoid.mediapicker.EXTRA_INTENT"
        const val EXTRA_REQUEST_CODE = "iam.thevoid.mediapicker.EXTRA_REQUEST_CODE"
        const val EXTRA_PHOTO_MAX_SIZE = "iam.thevoid.mediapicker.EXTRA_PHOTO_MAX_SIZE"
        const val EXTRA_PHOTO_MAX_PIXEL_WIDTH = "iam.thevoid.mediapicker.EXTRA_PHOTO_MAX_PIXEL_WIDTH"
        const val EXTRA_PHOTO_MAX_PIXEL_HEIGHT = "iam.thevoid.mediapicker.EXTRA_PHOTO_MAX_PIXEL_HEIGHT"
        const val EXTRA_VIDEO_MAX_SIZE = "iam.thevoid.mediapicker.EXTRA_VIDEO_MAX_SIZE"
        const val EXTRA_VIDEO_MAX_DURATION = "iam.thevoid.mediapicker.EXTRA_VIDEO_MAX_DURATION"
        const val EXTRA_VIDEO_QUALITY = "iam.thevoid.mediapicker.EXTRA_VIDEO_QUALITY"
    }
}