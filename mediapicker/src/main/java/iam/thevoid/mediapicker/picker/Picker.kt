package iam.thevoid.mediapicker.picker

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.annotation.StringRes
import iam.thevoid.ae.asActivity
import iam.thevoid.ae.asFragmentActivity
import iam.thevoid.ae.string
import iam.thevoid.mediapicker.R
import iam.thevoid.mediapicker.bus.MediaPickerBus
import iam.thevoid.mediapicker.bus.SelectAppBus
import iam.thevoid.mediapicker.chooser.IntentData
import iam.thevoid.mediapicker.chooser.MediaPickSelectAppDialog
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

    abstract fun request(context: Context): T

    abstract fun onResult(uri: Uri)

    abstract fun onEmptyResult()

    internal fun onDismiss() {
        onDismissListener?.onDismiss()
    }

    internal fun onImagePicked(uri: Uri?) = uri?.also(::onResult) ?: onEmptyResult()

    internal fun onAppSelect(context: Context, intentData: IntentData?) =
            intentData?.also { startImagePick(context, it.intent, it.requestCode) }

    protected fun startSelection(context: Context) {
        SelectAppBus.attachMediaPicker(this)
        if (onlyOneAppCanHandleRequest(context)) {
            val purpose = purposes[0]
            startImagePick(context, purpose.getIntent(context, bundle), purpose.requestCode)
            return
        }
        MediaPickSelectAppDialog.showForResult(context, intentData(context), chooserTitle
                ?: context.string(chooserTitleResource))
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

    protected fun needsPermissions(): Array<String> {
        val permissions: MutableList<String> = ArrayList()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (purposes.any { it is Take.Photo || it is Take.Video }) {
            permissions.add(Manifest.permission.CAMERA)
        }
        return permissions.toTypedArray()
    }

    private fun onlyOneAppCanHandleRequest(context: Context): Boolean = purposes.map {
        IntentUtils.getResolveInfoList(context.asActivity().packageManager,
                it.getIntent(context, bundle))
    }.flatten().size == 1

    abstract class Builder<T, Picker : iam.thevoid.mediapicker.picker.Picker<T>> {
        private val purposes: MutableSet<Purpose> = hashSetOf(Pick.Image)
        private var onDismissListener: OnDismissListener? = null
        private var videoMaxDuration = Duration(15, TimeUnit.SECONDS)
        private var photoMaxResolution = Resolution(3000, 3000)
        private var photoMaxSize = MemorySize(5, SizeUnit.MEGABYTE)
        private var videoMaxSize = MemorySize(10, SizeUnit.MEGABYTE)
        private var appChooserTitleResource = R.string.default_chooser_title
        private var appChooserTitle: String? = null

        protected abstract fun create(): Picker

        fun pick(vararg purpose: Pick) = apply {
            if (purpose.contains(Pick.Image) && purpose.contains(Pick.Video))
                purposes.add(Purpose.Hidden.Gallery)
            purposes.addAll(purpose)
        }

        fun take(vararg purpose: Take) =
                apply { purposes.addAll(purpose) }

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
        const val EXTRA_INTENT = "EXTRA_INTENT"
        const val EXTRA_REQUEST_CODE = "EXTRA_REQUEST_CODE"
        const val EXTRA_PHOTO_MAX_SIZE = "EXTRA_PHOTO_MAX_SIZE"
        const val EXTRA_VIDEO_MAX_SIZE = "EXTRA_VIDEO_MAX_SIZE"
        const val EXTRA_PHOTO_MAX_PIXEL_WIDTH = "EXTRA_PHOTO_MAX_PIXEL_WIDTH"
        const val EXTRA_PHOTO_MAX_PIXEL_HEIGHT = "EXTRA_PHOTO_MAX_PIXEL_HEIGHT"
        const val EXTRA_VIDEO_MAX_DURATION = "EXTRA_VIDEO_MAX_DURATION"
    }
}