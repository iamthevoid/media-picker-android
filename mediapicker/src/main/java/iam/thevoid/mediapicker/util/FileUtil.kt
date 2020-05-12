package iam.thevoid.mediapicker.util

import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import iam.thevoid.ae.asActivity
import iam.thevoid.e.safe
import java.io.File

/**
 * Created by iam on 16.04.17.
 */
object FileUtil {
    private val FILEPATH_KEY = FileUtil::class.java.name + ".FILEPATH_KEY"

    fun temp(context: Context): String = context.filesDir.path + "/temp"

    private val videoExtensions = listOf(
            ".3gp",
            ".mp4",
            ".ts",
            ".webm",
            ".mkv",
            ".mov"
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
        return ext != null && ext.isNotEmpty() && videoExtensions.contains(ext.toLowerCase())
    }

    fun isGifExt(ext: String?): Boolean {
        return ext != null && ext.isNotEmpty() && ".gif" == ext.toLowerCase()
    }

    fun Uri.extension(context: Context): String {
        val mimeTypeMap = MimeTypeMap.getSingleton()
        val contentResolver = context.contentResolver
        val type = contentResolver.getType(this)
        val extensionFromMimeType = mimeTypeMap.getExtensionFromMimeType(type)
        return "." + (extensionFromMimeType ?: MimeTypeMap.getFileExtensionFromUrl(path))
    }

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
                    val contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id))
                    return getDataColumn(context, contentUri, null, null)
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
}