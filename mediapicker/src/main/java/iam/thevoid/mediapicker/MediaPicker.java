package iam.thevoid.mediapicker;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import iam.thevoid.mediapicker.builder.ImageIntentBuilder;
import iam.thevoid.mediapicker.builder.PhotoIntentBuilder;
import iam.thevoid.mediapicker.builder.VideoIntentBuilder;
import iam.thevoid.mediapicker.chooser.ChooseAppDialog;
import iam.thevoid.mediapicker.chooser.IntentData;
import iam.thevoid.mediapicker.util.DateManager;
import iam.thevoid.mediapicker.util.SecurityStore;
import iam.thevoid.mediapicker.util.SizeUnit;

/**
 * Created by iam on 03.04.17.
 */

public class MediaPicker {

    private static final String TAG = MediaPicker.class.getSimpleName();

    private static final String PHOTO_PATH_PATTERN = "photo_%s.png";
    private static final String PHOTO_DATE_PATTERN = "yyyy_MM_dd_HH_mm_ss";

    public static final int REQUEST_TAKE_PHOTO = 0x1111;
    public static final int REQUEST_TAKE_VIDEO = 0x2222;
    public static final int REQUEST_PICK_IMAGE = 0x3333;
    public static final int REQUEST_PICK_VIDEO = 0x4444;
    public static final int REQUEST_RECOGNIZE_ = 0x5555;

    public static final void pickImage(Activity activity, With... with) {
        if (with == null || with.length == 0) {
            return;
        }

        if (with.length == 2) {
            List<With> withs = Arrays.asList(with);
            if (withs.contains(With.IMAGE_PICKER) && withs.contains(With.VIDEO_AND_IMAGE_PICKER)) {
                Intent intent = new ImageIntentBuilder()
                        .setLocalOnly(true)
                        .setMimetype(ImageIntentBuilder.Mimetype.BOTH_IMAGE_AND_VIDEO)
                        .build();
                activity.startActivityForResult(intent, REQUEST_PICK_IMAGE);
                return;
            }
        }

        if (with.length > 1) {
            IntentData[] intentDatas = new IntentData[with.length];
            for (int i = 0; i < with.length; i++) {
                intentDatas[i] = new IntentData(getIntent(activity, with[i]), getRequestCode(with[i]), getTitle(with[i]));
            }
            ChooseAppDialog.showForResult(activity, ((AppCompatActivity) activity).getSupportFragmentManager(), -1, intentDatas);
            return;
        }

        activity.startActivityForResult(getIntent(activity, with[0]), getRequestCode(with[0]));
    }

    public static final void pickImageForRecognize(Activity activity) {
        activity.startActivityForResult(getIntent(activity, With.PHOTO_CAMERA), REQUEST_RECOGNIZE_);
    }

    public static final String onActivityResult(Context context, int requestCode, int resultCode, Intent data) {
        return onActivityResult(context, requestCode, resultCode, data, null);
    }

    public static final String onActivityResult(Context context, int requestCode, int resultCode, Intent data,
                                                OnRecognizeListener orl) {

        if (requestCode == REQUEST_TAKE_PHOTO) {

            return generatePathForPhotoIntent(context);
        } else if (requestCode == REQUEST_RECOGNIZE_) {

            if (orl != null) {
                if (resultCode != Activity.RESULT_OK) {
                    orl.onRecognizeDismiss();
                } else {
                    orl.onRecognizeSuccess(generatePathForPhotoIntent(context));
                }
            }
            return null;
        }

        if (data == null) {
            return null;
        }

        switch (requestCode) {

            case REQUEST_PICK_IMAGE:
            case REQUEST_PICK_VIDEO:
                String path = generateLocalPathForGallery(context, data.getData(), requestCode);

                if (isVideo(path) && isVideoSizeLimitExceed(context, path)) {
                    return null;
                }

                return path;

            case REQUEST_TAKE_VIDEO:
                return generateLocalPathForCameraMedia(context, data.getData());
        }

        return null;
    }

    private static boolean isVideoSizeLimitExceed(Context context, String path) {
        long size = new File(path).length();
        if (size > 10L * SizeUnit.MEGABYTE.getBytes()) {
            Toast.makeText(context, R.string.video_size_limit_exceed, Toast.LENGTH_LONG).show();
            return true;
        }

        return false;
    }

    private static String generatePathForPhotoIntent(Context context) {
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), getPhotoPath());

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "generatePathForPhotoIntent: " + file.exists());
        }

        clearPhotoPath();

        return file.exists() ? file.getAbsolutePath() : null;
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

    private static String generateLocalPathForGallery(Context context, Uri uri, int requestCode) {
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
    private static String generateFromKitkat(Uri uri, Context context) {
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
    private static String generateVideoFromKitkat(Uri uri, Context context) {
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

    private static int getRequestCode(With with) {
        switch (with) {

            case IMAGE_PICKER:
                return REQUEST_PICK_IMAGE;
            case VIDEO_AND_IMAGE_PICKER:
                return REQUEST_PICK_VIDEO;
            case PHOTO_CAMERA:
                return REQUEST_TAKE_PHOTO;
            case VIDEO_CAMERA:
                return REQUEST_TAKE_VIDEO;
            default:
                return -1;
        }
    }

    private static int getTitle(With with) {
        switch (with) {

            case IMAGE_PICKER:
                return R.string.image_pick;
            case VIDEO_AND_IMAGE_PICKER:
                return R.string.video_pick;
            case PHOTO_CAMERA:
                return R.string.take_photo;
            case VIDEO_CAMERA:
                return R.string.take_video;
            default:
                return -1;
        }
    }

    private static Intent getIntent(Context context, With with) {
        switch (with) {
            case IMAGE_PICKER:
                return new ImageIntentBuilder()
                        .setLocalOnly(true)
                        .setMimetype(ImageIntentBuilder.Mimetype.IMAGE)
                        .build();
            case VIDEO_AND_IMAGE_PICKER:
                return new ImageIntentBuilder()
                        .setLocalOnly(true)
                        .setMimetype(ImageIntentBuilder.Mimetype.VIDEO)
                        .build();
            case PHOTO_CAMERA:

                storePhotoPath(String.format(PHOTO_PATH_PATTERN,
                        DateManager.formatDateToString(PHOTO_DATE_PATTERN, new Date(DateManager.getTime()))));

                return new PhotoIntentBuilder()
                        .setPhotoOutput(context, getPhotoPath())
                        .build();
            case VIDEO_CAMERA:
                return new VideoIntentBuilder()
                        .setVideoDuration(15)
                        .setVideoFileSize(10, SizeUnit.MEGABYTE)
                        .setVideoQuality(VideoIntentBuilder.VideoQuality.HIGH)
                        .build();
            default:
                return null;

        }
    }

    private static void storePhotoPath(String photoPath) {
        SecurityStore.setMediaPickerPhotoPath(photoPath);
    }

    private static String getPhotoPath() {
        return SecurityStore.getMediaPickerPhotoPath();
    }

    private static void clearPhotoPath() {
        SecurityStore.deleteMediaPickerPhotoPath();
    }


    public interface OnRecognizeListener {
        void onRecognizeDismiss();

        void onRecognizeSuccess(String filepath);
    }

    private static final List<String> videoExtensions = Arrays.asList(
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

    public enum With {
        PHOTO_CAMERA,
        VIDEO_CAMERA,
        IMAGE_PICKER,
        VIDEO_AND_IMAGE_PICKER,
    }
}
