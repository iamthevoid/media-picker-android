package iam.thevoid.mediapicker.picker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.StringRes
import iam.thevoid.mediapicker.R
import iam.thevoid.mediapicker.builder.ImageIntentBuilder
import iam.thevoid.mediapicker.builder.PhotoIntentBuilder
import iam.thevoid.mediapicker.builder.VideoIntentBuilder
import iam.thevoid.mediapicker.chooser.IntentData
import iam.thevoid.mediapicker.picker.metrics.SizeUnit

/**
 * Created by iam on 14/08/2017.
 */
sealed class Purpose {

    companion object {
        const val REQUEST_PICK_GALLERY = 0x999
        const val REQUEST_TAKE_PHOTO = 0x888
        const val REQUEST_TAKE_VIDEO = 0x777
        const val REQUEST_PICK_IMAGE = 0x666
        const val REQUEST_PICK_VIDEO = 0x555
    }

    open val title
        get() = -1

    abstract val requestCode: Int

    abstract fun getIntent(context: Context, data: Bundle): Intent

    fun getIntentData(context: Context, bundle: Bundle): IntentData =
            IntentData(getIntent(context, bundle), requestCode, title)

    sealed class Take(private val pickerTitle: Int) : Purpose() {

        override val title: Int
            get() = pickerTitle

        class Photo(@StringRes pickerTitle: Int = R.string.take_photo) : Take(pickerTitle) {
            override val requestCode: Int
                get() = REQUEST_TAKE_PHOTO

            override fun getIntent(context: Context, data: Bundle): Intent =
                    PhotoIntentBuilder().build(context)
        }

        class Video(@StringRes pickerTitle: Int = R.string.take_video) : Take(pickerTitle) {
            override fun getIntent(context: Context, data: Bundle): Intent = VideoIntentBuilder()
                    .setVideoDuration(data.getLong(Picker.EXTRA_VIDEO_MAX_DURATION, 0))
                    .setVideoFileSize(data.getLong(Picker.EXTRA_VIDEO_MAX_SIZE), SizeUnit.BYTE)
                    .setVideoQuality(VideoIntentBuilder.VideoQuality.HIGH)
                    .build()

            override val requestCode: Int
                get() = REQUEST_TAKE_VIDEO
        }
    }

    sealed class Pick : Purpose() {

        object Image : Pick() {
            override fun getIntent(context: Context, data: Bundle): Intent = ImageIntentBuilder()
                    .setLocalOnly(false)
                    .setMimetype(ImageIntentBuilder.Mimetype.IMAGE)
                    .build()

            override val requestCode: Int
                get() = REQUEST_PICK_IMAGE
        }

        object Video : Pick() {
            override fun getIntent(context: Context, data: Bundle): Intent = ImageIntentBuilder()
                    .setLocalOnly(false)
                    .setMimetype(ImageIntentBuilder.Mimetype.VIDEO)
                    .build()

            override val requestCode: Int
                get() = REQUEST_PICK_VIDEO
        }
    }

    sealed class Hidden : Purpose() {
        object Gallery : Hidden() {
            override val requestCode
                get(): Int = REQUEST_PICK_GALLERY

            override fun getIntent(context: Context, data: Bundle): Intent = ImageIntentBuilder()
                    .setLocalOnly(false)
                    .setMimetype(ImageIntentBuilder.Mimetype.BOTH_IMAGE_AND_VIDEO)
                    .build()
        }
    }
}