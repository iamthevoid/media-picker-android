package iam.thevoid.mediapicker.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import iam.thevoid.mediapicker.R;
import iam.thevoid.mediapicker.rxmediapicker.metrics.SizeUnit;

/**
 * Created by iam on 16.04.17.
 */

public class FileUtil {

    public static String temp(Context context) {
        return context.getFilesDir().getPath() + "/temp";
    }

    public static final List<String> videoExtensions = Arrays.asList(
            ".3gp",
            ".mp4",
            ".ts",
            ".webm",
            ".mkv",
            ".mov"
    );

    @NonNull
    public static String getExtension(String path) {
        return path == null ? "" : path.substring(path.lastIndexOf("."));
    }

    public static boolean isVideo(String path) {
        return isVideoExt(getExtension(path));
    }

    public static boolean isVideoExt(String ext) {
        return ext.length() > 0 && videoExtensions.contains(ext.toLowerCase());
    }

    public static boolean isGif(String path) {
        return isGifExt(getExtension(path));
    }

    public static boolean isGifExt(String ext) {
        return ext.length() > 0 && ".gif".equals(ext.toLowerCase());
    }

    public static String extension(Context context, Uri uri) {
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        ContentResolver contentResolver = context.getContentResolver();

        String type = contentResolver.getType(uri);

        String extensionFromMimeType = mimeTypeMap.getExtensionFromMimeType(type);
        return "." + (extensionFromMimeType == null ?
                (uri == null ? "" :
                        MimeTypeMap.getFileExtensionFromUrl(uri.getPath())) : extensionFromMimeType);
    }

    public static boolean isVideoSizeLimitExceed(Context context, String path) {
        long size = new File(path).length();
        if (size > 10L * SizeUnit.MEGABYTE.getBytes()) {
            Toast.makeText(context, R.string.video_size_limit_exceed, Toast.LENGTH_LONG).show();
            return true;
        }

        return false;
    }

    public static String getPath(final Context context, final Uri uri) throws Exception {

        if (uri == null) {
            return null;
        }

        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri)) {
// ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                try {
                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
                } catch (Exception e) {
                    throw new Exception("There are wrong docId: " + docId, e);
                }

// TODO handle non-primary volumes
            }
// DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
// MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs;
                try {
                    selectionArgs = new String[]{
                            split[1]
                    };
                } catch (Exception e) {
                    throw new Exception("There are wrong docId: " + docId, e);
                }

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
// MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
// File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
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
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

}
