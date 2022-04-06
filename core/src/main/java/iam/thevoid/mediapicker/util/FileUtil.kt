package iam.thevoid.mediapicker.util

import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Looper
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.graphics.scale
import iam.thevoid.ae.asActivity
import iam.thevoid.e.remove
import iam.thevoid.e.safe
import iam.thevoid.mediapicker.picker.options.ImageOptions
import iam.thevoid.mediapicker.picker.options.VideoOptions
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Created by iam on 16.04.17.
 */
object FileUtil {
    private val FILEPATH_KEY = FileUtil::class.java.name + ".FILEPATH_KEY"

    fun temp(context: Context): String = context.filesDir.absolutePath + "/temp"

    private val videoExtensions = listOf(
            "3gp",
            "mp4",
            "ts",
            "webm",
            "mkv",
            "mov"
    )

    @JvmStatic
    fun storePhotoPath(context: Context, path: String) =
            context.asActivity()
                    .getPreferences(Activity.MODE_PRIVATE)
                    .edit()
                    .putString(FILEPATH_KEY, path).apply()

    fun getPhotoPath(context: Context?): String =
            context?.asActivity()
                    ?.getPreferences(Activity.MODE_PRIVATE)
                    ?.getString(FILEPATH_KEY, "").safe()

    private fun clearPhotoPath(context: Context?) =
            context?.asActivity()
                    ?.getPreferences(Activity.MODE_PRIVATE)
                    ?.edit()?.remove(FILEPATH_KEY)?.apply()

    fun generatePathForPhotoIntent(context: Context?): String? {
        val file = File(getPhotoPath(context))
        clearPhotoPath(context)
        while (!file.canRead());
        return if (file.exists()) file.absolutePath else null
    }

    fun isVideoExt(ext: String?): Boolean {
        return !ext.isNullOrBlank() && videoExtensions.contains(ext.toLowerCase().replace(".", ""))
    }

    fun isGifExt(ext: String?): Boolean {
        return !ext.isNullOrBlank() && "gif" == ext.toLowerCase().replace(".", "")
    }

    fun Uri.mimeType(context: Context): String? =
            context.contentResolver.getType(this)

    fun Uri.extension(context: Context): String =
            ".${MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType(context))
                    ?: MimeTypeMap.getFileExtensionFromUrl(path)}"

    internal fun applyOptions(context: Context, uri: Uri, imageOptions: ImageOptions?, videoOptions: VideoOptions?): Uri =
            imageOptions?.takeIf { uri.isImage(context) }?.let { uri.resize(context, it).compress(context, it) }
                    ?: videoOptions?.takeIf { uri.isVideo(context) }?.let { uri.resize(context, it) }
                    ?: uri

    @Throws(Exception::class)
    fun getPath(context: Context, uri: Uri?): String? {
        if (uri == null) {
            return null
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri)) {
            when {
                isExternalStorageDocument(uri) -> {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    try {
                        if ("primary".equals(type, ignoreCase = true)) {
                            return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                        }
                    } catch (e: Exception) {
                        throw Exception("There are wrong docId: $docId", e)
                    }
                }
                isDownloadsDocument(uri) -> {
                    val id = DocumentsContract.getDocumentId(uri)
                    return when {
                        id.startsWith("raw:") -> id.remove("raw:")
                        id.startsWith("msf:") -> getImagePathFromMsfURI(context, uri)
                        else -> {
                            val contentUri = ContentUris.withAppendedId(
                                    Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id))
                            getDataColumn(context, contentUri, null, null)
                        }
                    }
                }
                isMediaDocument(uri) -> {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val contentUri: Uri = when (split[0]) {
                        "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        else -> Uri.EMPTY
                    }

                    val selection = "_id=?"
                    val selectionArgs: Array<String>
                    selectionArgs = try {
                        arrayOf(split[1])
                    } catch (e: Exception) {
                        throw Exception("There are wrong docId: $docId", e)
                    }
                    return getDataColumn(context, contentUri, selection, selectionArgs)
                }
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            return getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    private fun getImagePathFromMsfURI(context: Context, uri: Uri): String? = with(context.contentResolver) {
        query(uri, null, null, null, null)
                ?.use { cursor ->
                    cursor.moveToFirst()
                    cursor.getString(0)
                            .run { substring(lastIndexOf(":") + 1) }
                            .let { documentId ->
                                query(
                                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                        null,
                                        MediaStore.Images.Media._ID + " = ? ",
                                        arrayOf(documentId),
                                        null
                                )?.use {
                                    it.moveToFirst()
                                    it.getString(it.getColumnIndex(MediaStore.Images.Media.DATA))
                                }
                            }
                }
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private fun getDataColumn(context: Context, uri: Uri, selection: String?,
                              selectionArgs: Array<String>?): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(
                column
        )
        try {
            cursor = context.contentResolver.query(uri, projection, selection, selectionArgs,
                    null)
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(columnIndex)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        return null
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * Collect info about file and resize it if it declared in options
     */
    private fun Uri.resize(context: Context, options: ImageOptions): Uri {
        if (Looper.getMainLooper().thread == Thread.currentThread()) {
            throw RuntimeException("you can not apply image options in Main Thread")
        }

        val targetResolution = options.maxResolution.takeIf { it.width > 0 || it.height > 0 }
                ?: return this

        val path = getPath(context, this) ?: return this

        val originBitmap = BitmapFactory.decodeFile(path)
        val originHeight = originBitmap.height
        val originWidth = originBitmap.width

        // No need to resize
        if (options.preserveRatio && originHeight <= targetResolution.height && originWidth <= targetResolution.width) {
            return this
        }

        val targetHeight = targetResolution.height.takeIf { it > 0 } ?: originHeight
        val targetWidth = targetResolution.width.takeIf { it > 0 } ?: originWidth

        val targetBitmap = if (options.preserveRatio) {
            // When preserve ratio we constrained by resolution, so we must choose scale factor
            // which guarantee image does fit both target width and height
            val widthScaleFactor = targetWidth.toDouble() / originWidth.toDouble()
            val heightScaleFactor = targetHeight.toDouble() / originHeight.toDouble()

            // Desired scale factor is min of found
            val targetScaleFactor = min(widthScaleFactor, heightScaleFactor)
            originBitmap.scale(
                    (originWidth.toDouble() * targetScaleFactor).toInt(),
                    (originHeight.toDouble() * targetScaleFactor).toInt()
            )
        } else {
            originBitmap.scale(targetWidth, targetHeight)
        }


        return Uri.fromFile(targetBitmap.writeToCache(context, this, path))
    }

    /**
     * Collect info about file and resize it to fit size
     */
    private fun Uri.compress(context: Context, options: ImageOptions): Uri {
        if (options.maxSize.bytes < 0)
            return this

        val path = getPath(context, this) ?: return this

        val originFile = File(path)

        if (originFile.length() <= options.maxSize.bytes)
            return this

        val originBitmap = BitmapFactory.decodeFile(path)
        val originHeight = originBitmap.height
        val originWidth = originBitmap.width

        val scale = sqrt(options.maxSize.bytes.toDouble() / originFile.length().toDouble())

        val targetBitmap = originBitmap.scale(
                (originWidth.toDouble() * scale).toInt(),
                (originHeight.toDouble() * scale).toInt()
        )

        val targetFile = targetBitmap.writeToCache(context, this, path)

        if (targetFile.length() > options.maxSize.bytes)
            return Uri.fromFile(targetFile).compress(context, options)

        return Uri.fromFile(targetFile)
    }

    private fun Bitmap.writeToCache(context: Context, uri: Uri, path: String): File {
        val targetDir = File(context.cacheDir, "picker_images")
        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }
        val fileExtension = uri.extension(context)
        val filename = path.split("/").last().remove(fileExtension)
        val targetFile = File(targetDir, "$filename.png")
        if (targetFile.exists())
            targetFile.delete()

        FileOutputStream(targetFile).use {
            compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        return targetFile
    }

    // TODO Add applying options to video pick. Take video applies options with intent
    @Suppress("UNUSED_PARAMETER")
    fun Uri.resize(context: Context, options: VideoOptions): Uri = this

    fun Uri.isImage(context: Context) = !isVideoExt(extension(context))

    fun Uri.isVideo(context: Context) = isVideoExt(extension(context))

    fun copyFileToAppDir(context: Context, uri: Uri): File? {
        val returnCursor: Cursor = context.contentResolver.query(
            uri, arrayOf(
                OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE
            ), null, null, null
        ) ?: return null

        /*
         * Get the column indexes of the data in the Cursor,
         *     * move to the first row in the Cursor, get the data,
         *     * and display it.
         * */
        val nameIndex: Int = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex: Int = returnCursor.getColumnIndex(OpenableColumns.SIZE)
        returnCursor.moveToFirst()
        val name: String = returnCursor.getString(nameIndex)
        val size = returnCursor.getLong(sizeIndex).toString()
        val output = File("${context.cacheDir}/$name")
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(output)
            var read = 0
            val bufferSize = 1024
            val buffers = ByteArray(bufferSize)
            while (inputStream?.read(buffers).also { read = it ?: -1 } != -1) {
                outputStream.write(buffers, 0, read)
            }
            inputStream?.close()
            outputStream.close()
        } catch (e: Exception) {
            Log.e("Exception", e.message.orEmpty())
        } finally {
            returnCursor.close()
        }
        return File(output.path)
    }

}