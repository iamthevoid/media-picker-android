package iam.thevoid.mediapicker.builder

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import iam.thevoid.mediapicker.util.FileUtil
import iam.thevoid.mediapicker.util.FileUtil.storePhotoPath
import iam.thevoid.mediapicker.util.currentDateFilename
import java.io.File

class PhotoIntentBuilder {

    fun build(context: Context) =
            with(context) {
                getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                        ?: File(FileUtil.temp(this)).apply { if (!exists()) mkdirs() }
            }.let { directory ->
                File(directory, currentDateFilename(".jpg")).run {
                    if (exists())
                        delete()

                    storePhotoPath(context, absolutePath)

                    when {
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ->
                            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", this)
                        else -> Uri.fromFile(this)
                    }.let {
                        Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                                .putExtra(PHOTO_IMAGE_PATH, absolutePath)
                                .putExtra(MediaStore.EXTRA_OUTPUT, it)
                    }
                }
            }

    companion object {
        private const val PHOTO_IMAGE_PATH = "PHOTO_IMAGE_PATH"
    }
}