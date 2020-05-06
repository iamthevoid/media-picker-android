@file:JvmName(TAG)

package iam.thevoid.mediapicker.rxmediapicker

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import iam.thevoid.mediapicker.exception.ExtractBitmapException
import iam.thevoid.mediapicker.util.Editor
import iam.thevoid.mediapicker.util.FileUtil
import java.io.*
import java.util.*

private const val TAG = "UriTransformer"

@Throws(IOException::class)
fun Uri.toBitmap(context: Context): Bitmap =
        MediaStore.Images.Media.getBitmap(context.contentResolver, this)

@Throws(IOException::class)
fun Uri.toFile(context: Context, file: File? = null): File {
    val outputFile = file ?: File(filepath(context))
    let(context.contentResolver::openInputStream)?.use { inputStream ->
        if (!outputFile.exists()) {
            File(outputFile.parent).mkdirs()
            outputFile.createNewFile()
        }
        FileOutputStream(file).use { outputStream ->
            val buf = ByteArray(10 * 1024)
            var len: Int
            while (inputStream.read(buf).also { len = it } > 0) {
                outputStream.write(buf, 0, len)
            }
        }
    }
    return outputFile
}

fun bitmapToUriConverter(mBitmap: Any?): Uri? {
    if (mBitmap !is Bitmap) {
        throw ExtractBitmapException("Fetched data is not a bitmap. Fetched data is ${mBitmap?.let { it::class.java.simpleName }}")
    }
    var tempFile: File? = null

    Environment.getExternalStorageDirectory().also { externalStorageDir ->
        File(externalStorageDir.absolutePath + "/.temp/").also { tempDir ->
            tempDir.mkdirs()
            try {
                tempFile = File.createTempFile("Image"
                        + Random().nextInt(), ".jpg", tempDir)
            } catch (e: IOException) {
                throw ExtractBitmapException(e)
            }
        }
    }

    ByteArrayOutputStream().use { byteArrayOutputStream ->
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val bitmapData = byteArrayOutputStream.toByteArray()
        if (tempFile == null) {
            throw ExtractBitmapException("Can not create temporary file to write bitmap")
        }
        //write the bytes in file
        try {
            FileOutputStream(tempFile).use { fos ->
                fos.write(bitmapData)
                fos.flush()
            }
        } catch (e: IOException) {
            throw ExtractBitmapException(e)
        }
    }

    return tempFile?.let(Uri::fromFile)
}

fun Uri.filepath(context: Context) = FileUtil.temp(context) + "/" +
        Editor.currentDateFilename(filenamePrefix(context, this), FileUtil.extension(context, this))

private fun filenamePrefix(context: Context, uri: Uri): String {
    val ext = FileUtil.extension(context, uri)
    if (FileUtil.isVideoExt(ext)) {
        return "video"
    }
    return if (FileUtil.isGifExt(ext)) {
        "anim"
    } else "image"
}