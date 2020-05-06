package iam.thevoid.mediapicker.rxmediapicker

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import iam.thevoid.mediapicker.chooser.IntentData
import iam.thevoid.mediapicker.chooser.MediaPickSelectAppDialog
import iam.thevoid.mediapicker.chooser.MediaPickSelectAppDialog.OnSelectAppCallback
import iam.thevoid.mediapicker.rxmediapicker.HiddenPickerFragment.Companion.getFragment
import iam.thevoid.mediapicker.rxmediapicker.Purpose.Pick
import iam.thevoid.mediapicker.rxmediapicker.Purpose.Take
import iam.thevoid.mediapicker.rxmediapicker.metrics.Duration
import iam.thevoid.mediapicker.rxmediapicker.metrics.MemorySize
import iam.thevoid.mediapicker.rxmediapicker.metrics.Resolution
import iam.thevoid.mediapicker.rxmediapicker.metrics.SizeUnit
import iam.thevoid.mediapicker.util.IntentUtils
import java.util.*
import java.util.concurrent.TimeUnit

abstract class Picker<T> protected constructor() : OnSelectAppCallback {

    private var purposes = ArrayList<Purpose>(listOf(Pick.IMAGE))
    private var onDismissListener: OnDismissListener? = null

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

    override fun onAppSelect(context: Context, intentData: IntentData) {
        startImagePick(context, intentData.intent, intentData.requestCode)
    }

    protected fun startSelection(context: Context) {
        if (onlyOneAppCanHandleRequest(context)) {
            val purpose = purposes[0]
            startImagePick(context, purpose.getIntent(context, bundle), purpose.requestCode())
            return
        }
        MediaPickSelectAppDialog.showForResult(context, intentData(context), this)
    }

    private fun intentData(context: Context): List<IntentData> =
            purposes.map { it.getIntentData(context, bundle) }

    private fun startImagePick(context: Context, intent: Intent, requestCode: Int) {
        PickerEventBus.attachMediaPicker(this)
        IntentUtils.getFragmentActivity(context).supportFragmentManager
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
        if (purposes.contains(Take.PHOTO) || purposes.contains(Take.VIDEO)) {
            permissions.add(Manifest.permission.CAMERA)
        }
        return permissions.toTypedArray()
    }

    private fun onlyOneAppCanHandleRequest(context: Context): Boolean {
        val resolveInfos: MutableList<ResolveInfo> = ArrayList()
        for (purpose in purposes) {
            resolveInfos.addAll(IntentUtils.getResolveInfoList(IntentUtils.getActivity(context).packageManager,
                    purpose.getIntent(context, bundle)))
        }
        return resolveInfos.size == 1
    }

    abstract class Builder<T, Picker : iam.thevoid.mediapicker.rxmediapicker.Picker<T>> {
        private val purposes: MutableSet<Purpose> = HashSet()
        private var onDismissListener: OnDismissListener? = null
        private var videoMaxDuration = Duration(15, TimeUnit.SECONDS)
        private var photoMaxResolution = Resolution(3000, 3000)
        private var photoMaxSize = MemorySize(5, SizeUnit.MEGABYTE)
        private var videoMaxSize = MemorySize(10, SizeUnit.MEGABYTE)

        fun pick(vararg purpose: Pick) = apply {
            if (purpose.contains(Pick.IMAGE) && purpose.contains(Pick.VIDEO)) {
                purposes.add(Purpose.Hidden.GALLERY)
            }
            purposes.addAll(purpose)
        }

        fun take(vararg purpose: Take) = apply {
            purposes.addAll(purpose)
        }

        fun onDismiss(onDismissListener: OnDismissListener) = apply {
            this.onDismissListener = onDismissListener
        }

        fun setVideoMaxDuration(videoMaxDuration: Duration) = apply {
            this.videoMaxDuration = videoMaxDuration
        }

        fun setPhotoMaxSize(photoMaxSize: MemorySize) = apply {
            this.photoMaxSize = photoMaxSize
        }

        fun setVideoMaxSize(videoMaxSize: MemorySize) = apply {
            this.videoMaxSize = videoMaxSize
        }

        fun setPhotoMaxResolution(photoMaxResolution: Resolution) = apply {
            this.photoMaxResolution = photoMaxResolution
        }

        protected abstract fun create(): Picker

        fun build(): Picker =
                create().apply {
                    purposes = ArrayList(this@Builder.purposes)
                    onDismissListener = this@Builder.onDismissListener
                    videoMaxDuration = this@Builder.videoMaxDuration.seconds
                    photoMaxSize = this@Builder.photoMaxSize.bytes
                    videoMaxSize = this@Builder.videoMaxSize.bytes
                    photoMaxPixelsHeight = this@Builder.photoMaxResolution.height
                    photoMaxPixelsWidth = this@Builder.photoMaxResolution.width
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