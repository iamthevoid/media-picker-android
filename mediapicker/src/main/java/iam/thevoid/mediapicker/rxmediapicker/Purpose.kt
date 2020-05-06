package iam.thevoid.mediapicker.rxmediapicker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import iam.thevoid.mediapicker.R
import iam.thevoid.mediapicker.builder.ImageIntentBuilder
import iam.thevoid.mediapicker.builder.PhotoIntentBuilder
import iam.thevoid.mediapicker.builder.VideoIntentBuilder
import iam.thevoid.mediapicker.chooser.IntentData
import iam.thevoid.mediapicker.rxmediapicker.metrics.SizeUnit

/**
 * Created by iam on 14/08/2017.
 */
interface Purpose {
    enum class Take : Purpose {
        PHOTO {
            override fun requestCode(): Int = REQUEST_TAKE_PHOTO

            override fun getIntent(context: Context, data: Bundle): Intent = PhotoIntentBuilder()
                    .build(context)

            override fun getIntentData(context: Context, bundle: Bundle): IntentData =
                    IntentData(getIntent(context, bundle), requestCode(), R.string.take_photo)
        },
        VIDEO {
            override fun getIntent(context: Context, data: Bundle): Intent = VideoIntentBuilder()
                    .setVideoDuration(data.getLong(Picker.EXTRA_VIDEO_MAX_DURATION, 0))
                    .setVideoFileSize(data.getLong(Picker.EXTRA_VIDEO_MAX_SIZE), SizeUnit.BYTE)
                    .setVideoQuality(VideoIntentBuilder.VideoQuality.HIGH)
                    .build()

            override fun requestCode(): Int = REQUEST_TAKE_VIDEO

            override fun getIntentData(context: Context, bundle: Bundle): IntentData =
                    IntentData(getIntent(context, bundle), requestCode(), R.string.take_video)
        }
    }

    enum class Pick : Purpose {
        IMAGE {
            override fun getIntent(context: Context, data: Bundle): Intent = ImageIntentBuilder()
                    .setLocalOnly(false)
                    .setMimetype(ImageIntentBuilder.Mimetype.IMAGE)
                    .build()

            override fun requestCode(): Int = REQUEST_PICK_IMAGE
        },
        VIDEO {
            override fun getIntent(context: Context, data: Bundle): Intent = ImageIntentBuilder()
                    .setLocalOnly(false)
                    .setMimetype(ImageIntentBuilder.Mimetype.VIDEO)
                    .build()

            override fun requestCode(): Int = REQUEST_PICK_VIDEO
        }
    }

    enum class Hidden : Purpose {
        GALLERY {
            override fun requestCode(): Int = REQUEST_PICK_GALLERY

            override fun getIntent(context: Context, data: Bundle): Intent = ImageIntentBuilder()
                    .setLocalOnly(false)
                    .setMimetype(ImageIntentBuilder.Mimetype.BOTH_IMAGE_AND_VIDEO)
                    .build()
        }
    }

    fun requestCode(): Int

    fun getIntent(context: Context, data: Bundle): Intent

    fun getIntentData(context: Context, bundle: Bundle): IntentData {
        return IntentData(getIntent(context, bundle), requestCode())
    }

    companion object {

        const val REQUEST_PICK_GALLERY = 0x999
        const val REQUEST_TAKE_PHOTO = 0x888
        const val REQUEST_TAKE_VIDEO = 0x777
        const val REQUEST_PICK_IMAGE = 0x666
        const val REQUEST_PICK_VIDEO = 0x555
    }
}