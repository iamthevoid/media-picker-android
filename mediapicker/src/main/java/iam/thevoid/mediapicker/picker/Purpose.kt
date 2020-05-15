package iam.thevoid.mediapicker.picker

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
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
        const val REQUEST_PICK_GALLERY = 0xBEEF
        const val REQUEST_TAKE_VIDEO = 0xABBA
        const val REQUEST_PICK_IMAGE = 0xACDC
        const val REQUEST_PICK_VIDEO = 0xDEAD
        const val REQUEST_TAKE_PHOTO = 0xBEDA
    }

    protected open val additionalPermissions = emptyList<String>()

    internal abstract val requestCode: Int

    internal abstract fun getIntent(context: Context, data: Bundle): Intent

    private fun permissions(): List<String> {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        permissions.addAll(additionalPermissions)
        return permissions
    }

    fun getIntentData(context: Context, bundle: Bundle, title: String? = null): IntentData =
            IntentData(getIntent(context, bundle), requestCode, title, permissions())

    sealed class Take : Purpose() {

        override val additionalPermissions: List<String>
            get() = listOf(Manifest.permission.CAMERA)

        object Photo : Take() {
            override val requestCode: Int
                get() = REQUEST_TAKE_PHOTO

            override fun getIntent(context: Context, data: Bundle): Intent =
                    PhotoIntentBuilder().build(context)
        }

        object Video : Take() {
            override fun getIntent(context: Context, data: Bundle): Intent = VideoIntentBuilder(
                    videoDuration = data.getLong(Picker.EXTRA_VIDEO_MAX_DURATION).takeIf { it > 0 },
                    videoFileSize = data.getLong(Picker.EXTRA_VIDEO_MAX_SIZE).takeIf { it > 0 }
                            ?.let { it * SizeUnit.BYTE.bytes },
                    videoQuality = data.getInt(Picker.EXTRA_VIDEO_MAX_SIZE)
            ).build()

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
        internal object Gallery : Hidden() {
            override val requestCode
                get(): Int = REQUEST_PICK_GALLERY

            override fun getIntent(context: Context, data: Bundle): Intent = ImageIntentBuilder()
                    .setLocalOnly(false)
                    .setMimetype(ImageIntentBuilder.Mimetype.BOTH_IMAGE_AND_VIDEO)
                    .build()
        }
    }
}