package iam.thevoid.mediapicker.builder;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.core.content.FileProvider;

import java.io.File;

import iam.thevoid.mediapicker.util.Editor;
import iam.thevoid.mediapicker.util.FileUtil;

public class PhotoIntentBuilder {

    private static final String PHOTO_IMAGE_PATH = "PHOTO_IMAGE_PATH";

    private int flags = 0;


    public PhotoIntentBuilder setFlags(int flags) {
        this.flags = flags;
        return this;
    }

    public Intent build(Context context) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (flags != 0) {
            intent.setFlags(flags);
        }

        String filename = Editor.currentDateFilename("", ".jpg");

        File file =
                new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), filename);

        if (file.exists()) {
            file.delete();
        }

        Uri photoOutput;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String packageName = context.getPackageName();
            photoOutput = FileProvider.getUriForFile(context, packageName + ".fileprovider", file);
        } else {
            photoOutput = Uri.fromFile(file);
        }

        FileUtil.storePhotoPath(context, file.getAbsolutePath());

        return new Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                .putExtra(PHOTO_IMAGE_PATH, file.getAbsolutePath())
                .putExtra(MediaStore.EXTRA_OUTPUT, photoOutput);
    }
}
