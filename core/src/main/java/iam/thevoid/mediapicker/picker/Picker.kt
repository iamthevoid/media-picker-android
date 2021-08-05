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
import iam.thevoid.mediapicker.picker.Purpose.Pick
import iam.thevoid.mediapicker.picker.Purpose.Take
import iam.thevoid.mediapicker.picker.fragment.HiddenPickerFragment
import iam.thevoid.mediapicker.picker.fragment.HiddenPickerFragment.Companion.getFragment
import iam.thevoid.mediapicker.picker.options.ImageOptions
import iam.thevoid.mediapicker.picker.options.VideoOptions
import iam.thevoid.mediapicker.util.FileUtil
import iam.thevoid.mediapicker.util.IntentUtils
import java.util.*
import kotlin.time.ExperimentalTime

abstract class Picker<T> protected constructor() {

    private var purposes = listOf<Purpose>()

    private var onDismissPickListener: OnDismissListener? = null

    private var onDismissAppSelectListener: OnDismissListener? = null

    @StringRes
    private var chooserTitleResource: Int = -1

    private var chooserTitle: String? = null

    @StringRes
    private var takePhotoChooserTitleResource: Int = -1

    private var takePhotoChooserTitle: String? = null

    @StringRes
    private var takeVideoChooserTitleResource: Int = -1

    private var takeVideoChooserTitle: String? = null

    private var imageOptions: ImageOptions? = null

    private var videoOptions: VideoOptions? = null

    @OptIn(ExperimentalTime::class)
    private val bundle: Bundle
        get() = Bundle().apply {
            videoOptions?.apply {
                maxDuration.inSeconds.toLong().takeIf { it > 0 }
                        ?.also { putLong(EXTRA_VIDEO_MAX_DURATION, it) }

                maxSize.takeIf { it.bytes > 0 }
                        ?.also { putLong(EXTRA_VIDEO_MAX_SIZE, it.bytes) }

                putInt(EXTRA_VIDEO_QUALITY, quality.code)
            }
        }


    protected abstract fun initStream(applyOptions: (Uri) -> Uri): T

    protected abstract fun requestPermissions(
            context: Context,
            permissions: List<String>,
            result: OnRequestPermissionsResult
    )

    protected abstract fun onResult(uri: Uri)

    protected abstract fun onEmptyResult()


    fun request(context: Context): T = initStream {
        FileUtil.applyOptions(context, it, imageOptions, videoOptions)
    }.also {
        SelectAppBus.attachMediaPicker(this)
        when {
            isOnlyOneAppCanHandleRequest(context) ->
                purposes.first()
                        .also { handleIntent(context, it.getIntentData(context, bundle)) }
            else -> {
                chooserTitle.safe(context.string(chooserTitleResource)).also { title ->
                    PickerSelectAppDialog
                            .showForResult(context, purposes.map { it.getIntentData(context, bundle) }, title)
                }
            }
        }
    }

    internal fun onAppSelect(context: Context, intentData: IntentData?) =
            intentData?.also { handleIntent(context, it) }

    internal fun onImagePicked(uri: Uri?) =
            uri?.also(::onResult) ?: onEmptyResult()

    internal fun onDismissSelectApp() {
        onDismissAppSelectListener?.onDismiss()
    }


    internal fun onImagePickDismissed() {
        onEmptyResult()
        onDismissPickListener?.onDismiss()
    }


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

    private fun startImagePick(context: Context, intent: Intent, requestCode: Int) {
        MediaPickerBus.attachMediaPicker(this)
        context.asFragmentActivity().supportFragmentManager
                .beginTransaction()
                .add(getFragment(intent, requestCode), HiddenPickerFragment::class.java.canonicalName)
                .commitAllowingStateLoss()
    }

    private fun isOnlyOneAppCanHandleRequest(context: Context): Boolean = purposes.map {
        IntentUtils.getResolveInfoList(context.asActivity().packageManager,
                it.getIntent(context, bundle))
    }.flatten().size == 1

    private fun Purpose.toIntentData(context: Context) =
            getIntentData(context, bundle, title(context))

    private fun Purpose.title(context: Context) = when (this) {
        Take.Photo -> takePhotoChooserTitle ?: takePhotoChooserTitleResource
                .takeIf { it > 0 }?.let { context.string(it) }
        Take.Video -> takeVideoChooserTitle ?: takeVideoChooserTitleResource
                .takeIf { it > 0 }?.let { context.string(it) }
        else -> null
    }

    abstract class Builder<T, Picker : iam.thevoid.mediapicker.picker.Picker<T>> {
        private var purposes: HashSet<Purpose> = hashSetOf()
        private var onDismissPickListener: OnDismissListener? = null
        private var onDismissAppSelectListener: OnDismissListener? = null
        private var imageOptions: ImageOptions? = null
        private var videoOptions: VideoOptions? = null
        private var appChooserTitleResource = R.string.default_chooser_title
        private var appChooserTitle: String? = null
        private var takePhotoAppChooserTitleResource = R.string.take_photo
        private var takePhotoAppChooserTitle: String? = null
        private var takeVideoAppChooserTitleResource = R.string.take_video
        private var takeVideoAppChooserTitle: String? = null

        protected abstract fun create(): Picker

        fun setTakeVideoOptions(options: VideoOptions) = apply {
            this.videoOptions = options
        }

        fun setImageOptions(options: ImageOptions) = apply {
            this.imageOptions = options
        }

        fun pick(vararg pickPurposes: Pick) = apply {
            when {
                pickPurposes.contains(Pick.Image) && pickPurposes.contains(Pick.Video) -> {
                    purposes.add(Purpose.Hidden.Gallery)
                    pickPurposes.filter { it !is Pick.Image && it !is Pick.Video }
                            .also { purposes.addAll(it) }
                }
                else -> {
                    purposes.addAll(pickPurposes)
                }
            }


        }

        fun take(vararg take: Take) = apply {
            purposes.addAll(take)
        }

        fun onDismissPick(onDismissListener: OnDismissListener) =
                apply { this.onDismissPickListener = onDismissListener }

        fun onDismissAppSelect(onDismissListener: OnDismissListener) =
                apply { this.onDismissAppSelectListener = onDismissListener }

        fun setChooserTitle(@StringRes titleResource: Int) = apply {
            appChooserTitleResource = titleResource
            appChooserTitle = null
        }

        fun setChooserTitle(title: String) = apply {
            appChooserTitleResource = -1
            appChooserTitle = title
        }

        fun setTakePhotoAppChooserTitle(@StringRes titleResource: Int = -1) = apply {
            takePhotoAppChooserTitleResource = titleResource
            takePhotoAppChooserTitle = null
        }

        fun setTakePhotoAppChooserTitle(title: String? = null) = apply {
            takePhotoAppChooserTitleResource = -1
            takePhotoAppChooserTitle = title
        }

        fun setTakeVideoAppChooserTitle(@StringRes titleResource: Int = -1) = apply {
            takeVideoAppChooserTitleResource = titleResource
            takeVideoAppChooserTitle = null
        }

        fun setTakeVideoAppChooserTitle(title: String? = null) = apply {
            takeVideoAppChooserTitleResource = -1
            takeVideoAppChooserTitle = title
        }

        fun build(): Picker =
                create().apply {
                    purposes = ArrayList(this@Builder.purposes)
                    onDismissPickListener = this@Builder.onDismissPickListener
                    onDismissAppSelectListener = this@Builder.onDismissAppSelectListener
                    imageOptions = this@Builder.imageOptions
                    videoOptions = this@Builder.videoOptions
                    chooserTitle = appChooserTitle
                    chooserTitleResource = appChooserTitleResource
                    takePhotoChooserTitle = takePhotoAppChooserTitle
                    takePhotoChooserTitleResource = takePhotoAppChooserTitleResource
                    takeVideoChooserTitle = takePhotoAppChooserTitle
                    takeVideoChooserTitleResource = takeVideoAppChooserTitleResource
                }
    }

    interface OnDismissListener {
        fun onDismiss()
    }

    companion object {

        private val TAG = Picker::class.java.simpleName

        const val EXTRA_INTENT = "iam.thevoid.mediapicker.EXTRA_INTENT"
        const val EXTRA_REQUEST_CODE = "iam.thevoid.mediapicker.EXTRA_REQUEST_CODE"
        const val EXTRA_VIDEO_MAX_SIZE = "iam.thevoid.mediapicker.EXTRA_VIDEO_MAX_SIZE"
        const val EXTRA_VIDEO_MAX_DURATION = "iam.thevoid.mediapicker.EXTRA_VIDEO_MAX_DURATION"
        const val EXTRA_VIDEO_QUALITY = "iam.thevoid.mediapicker.EXTRA_VIDEO_QUALITY"
    }
}