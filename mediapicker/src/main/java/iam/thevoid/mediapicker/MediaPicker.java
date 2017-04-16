package iam.thevoid.mediapicker;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import iam.thevoid.mediapicker.builder.ImageIntentBuilder;
import iam.thevoid.mediapicker.builder.PhotoIntentBuilder;
import iam.thevoid.mediapicker.builder.VideoIntentBuilder;
import iam.thevoid.mediapicker.chooser.ChooseAppDialog;
import iam.thevoid.mediapicker.chooser.IntentData;
import iam.thevoid.mediapicker.util.FileUtil;
import iam.thevoid.mediapicker.util.SizeUnit;

/**
 * Created by iam on 03.04.17.
 */

public final class MediaPicker {

    private MediaPicker() {
    }

    private static final int REQUEST_TAKE_PHOTO = 0x1111;
    private static final int REQUEST_TAKE_VIDEO = 0x2222;
    private static final int REQUEST_PICK_IMAGE = 0x3333;
    private static final int REQUEST_PICK_VIDEO = 0x4444;

    public static void pickImage(Activity activity, With... with) {
        if (with == null || with.length == 0) {
            return;
        }

        if (with.length > 1) {
            IntentData[] intentDatas = new IntentData[with.length];
            for (int i = 0; i < with.length; i++) {
                Intent intent = getIntent(with[i]);

                PackageManager pm = activity.getPackageManager();

                List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);

                if (resolveInfos == null) {
                    continue;
                }

                for (ResolveInfo resolveInfo : resolveInfos) {

                    ActivityInfo activityInfo = resolveInfo.activityInfo;

                    ComponentName name = new ComponentName(activityInfo.applicationInfo.packageName,
                            activityInfo.name);

                    Intent generated = new Intent(intent.getAction());

                    if (intent.getCategories() != null) {
                        for (String category : intent.getCategories()) {
                            generated.addCategory(category);
                        }
                    }

                    generated.setFlags(intent.getFlags());
                    generated.setComponent(name);

                    intentDatas[i] = new IntentData(generated, getRequestCode(with[i]), getTitle(with[i]));
                }
            }
            ChooseAppDialog.showForResult(activity, ((AppCompatActivity) activity).getSupportFragmentManager(), -1, intentDatas);
            return;
        }

        activity.startActivityForResult(getIntent(with[0]), getRequestCode(with[0]));
    }


    public static String onActivityResult(Context context, int requestCode, int resultCode,
                                          Intent data) {

        if (resultCode != Activity.RESULT_OK) {
            return null;
        }

        if (requestCode == REQUEST_TAKE_PHOTO) {
            return FileUtil.getPathFromPhoto(context, data);
        }

        if (data == null) {
            return null;
        }

        switch (requestCode) {

            case REQUEST_PICK_IMAGE:
            case REQUEST_PICK_VIDEO:
                String path = FileUtil.generateLocalPathForGallery(context, data.getData(), requestCode);

                if (FileUtil.isVideo(path) && FileUtil.isVideoSizeLimitExceed(context, path)) {
                    return null;
                }

                return path;

            case REQUEST_TAKE_VIDEO:
                return FileUtil.generateLocalPathForCameraMedia(context, data.getData());
        }

        return null;
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

    private static Intent getIntent(With with) {
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
                return new PhotoIntentBuilder()
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

    public enum With {
        PHOTO_CAMERA,
        VIDEO_CAMERA,
        IMAGE_PICKER,
        VIDEO_AND_IMAGE_PICKER,
    }
}
