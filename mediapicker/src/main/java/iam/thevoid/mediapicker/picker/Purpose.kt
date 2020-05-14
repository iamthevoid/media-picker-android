package iam.thevoid.mediapicker.picker

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
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
internal sealed class Purpose {

    companion object {
        const val REQUEST_TAKE_PHOTO = 0x888
        const val REQUEST_TAKE_VIDEO = 0x777
        const val REQUEST_PICK_IMAGE = 0x666
        const val REQUEST_PICK_VIDEO = 0x555
    }

    protected open val additionalPermissions = emptyList<String>()

    internal open val title
        get() = -1

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

    internal fun getIntentData(context: Context, bundle: Bundle): IntentData =
            IntentData(getIntent(context, bundle), requestCode, title, permissions())

    internal sealed class Take(private val pickerTitle: Int) : Purpose() {

        override val title: Int
            get() = pickerTitle

        override val additionalPermissions: List<String>
            get() = listOf(Manifest.permission.CAMERA)

        internal class Photo(@StringRes pickerTitle: Int) : Take(pickerTitle) {
            override val requestCode: Int
                get() = REQUEST_TAKE_PHOTO

            override fun getIntent(context: Context, data: Bundle): Intent =
                    PhotoIntentBuilder().build(context)
        }

        internal class Video(@StringRes pickerTitle: Int = R.string.take_video) : Take(pickerTitle) {
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

    internal sealed class Pick : Purpose() {

        internal object Image : Pick() {
            override fun getIntent(context: Context, data: Bundle): Intent = ImageIntentBuilder()
                    .setLocalOnly(false)
                    .setMimetype(ImageIntentBuilder.Mimetype.IMAGE)
                    .build()

            override val requestCode: Int
                get() = REQUEST_PICK_IMAGE
        }

        internal object Video : Pick() {
            override fun getIntent(context: Context, data: Bundle): Intent = ImageIntentBuilder()
                    .setLocalOnly(false)
                    .setMimetype(ImageIntentBuilder.Mimetype.VIDEO)
                    .build()

            override val requestCode: Int
                get() = REQUEST_PICK_VIDEO
        }
    }
}