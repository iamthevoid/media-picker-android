package iam.thevoid.mediapicker.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import iam.thevoid.mediapicker.R;

/**
 * Created by iam on 16.04.17.
 */

public class FileUtil {


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
        return videoExtensions.contains(getExtension(path).toLowerCase());
    }


    public static boolean isVideoSizeLimitExceed(Context context, String path) {
        long size = new File(path).length();
        if (size > 10L * SizeUnit.MEGABYTE.getBytes()) {
            Toast.makeText(context, R.string.video_size_limit_exceed, Toast.LENGTH_LONG).show();
            return true;
        }

        return false;
    }

    public static String generateLocalPathForCameraMedia(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);

            return path;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static String generateLocalPathForGallery(Context context, Uri uri, int requestCode) {
        String filePath = null;
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        if (isKitKat) {
            if (uri.toString().contains("video")) {
                filePath = generateVideoFromKitkat(uri, context);
            } else {
                filePath = generateFromKitkat(uri, context);
            }
        }

        if (filePath != null) {
            return filePath;
        }

        Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.MediaColumns.DATA}, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                filePath = cursor.getString(columnIndex);
            }
            cursor.close();
        }
        return filePath == null ? uri.getPath() : filePath;
    }


    @TargetApi(19)
    public static String generateFromKitkat(Uri uri, Context context) {
        String filePath = null;
        if (DocumentsContract.isDocumentUri(context, uri)) {
            String wholeID = DocumentsContract.getDocumentId(uri);

            String id = wholeID.split(":")[1];

            String[] column = {MediaStore.Images.Media.DATA};
            String sel = MediaStore.Images.Media._ID + "=?";

            Cursor cursor = context.getContentResolver().
                    query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            column, sel, new String[]{id}, null);
            int columnIndex = cursor.getColumnIndex(column[0]);

            if (cursor.moveToFirst()) {
                filePath = cursor.getString(columnIndex);
            }

            cursor.close();
        }

        return filePath;
    }

    @TargetApi(19)
    public static String generateVideoFromKitkat(Uri uri, Context context) {
        String filePath = null;
        if (DocumentsContract.isDocumentUri(context, uri)) {
            String wholeID = DocumentsContract.getDocumentId(uri);

            String id = wholeID.split(":")[1];

            String[] column = {MediaStore.Video.Media.DATA};
            String sel = MediaStore.Video.Media._ID + "=?";

            Cursor cursor = context.getContentResolver().
                    query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            column, sel, new String[]{id}, null);
            int columnIndex = cursor.getColumnIndex(column[0]);

            if (cursor.moveToFirst()) {
                filePath = cursor.getString(columnIndex);
            }

            cursor.close();
        }

        return filePath;
    }

    public static Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public static String getRealPathFromURI(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    public static String getPathFromPhoto(Context context, Intent data) {
        Bitmap photo = (Bitmap) data.getExtras().get("data");

        Uri tempUri = FileUtil.getImageUri(context, photo);

        File finalFile = new File(FileUtil.getRealPathFromURI(context, tempUri));

        return finalFile.getAbsolutePath();
    }
}
