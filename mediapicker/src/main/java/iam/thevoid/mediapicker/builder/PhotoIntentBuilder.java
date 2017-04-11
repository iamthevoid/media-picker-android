package iam.thevoid.mediapicker.builder;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;

/**
 * Created by iam on 03.04.17.
 */

public class PhotoIntentBuilder {

    private static final String TAG = PhotoIntentBuilder.class.getSimpleName();

    private Uri photoOutput;
    private int flags = 0;

    public PhotoIntentBuilder setPhotoOutput(Context context, String filename) {

        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), filename);

        if (file.exists()) {
            file.delete();
        }

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String packageName = context.getPackageName();
            photoOutput = FileProvider.getUriForFile(context, packageName + ".fileprovider", file);
//        } else {
//            photoOutput = Uri.fromFile(file);
//        }

        if (photoOutput == null) {
            Log.e(TAG, "setPhotoOutput: SIC!");
        }

        return this;
    }

    public PhotoIntentBuilder setFlags(int flags) {
        this.flags = flags;
        return this;
    }

    public Intent build() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (flags != 0) {
            intent.setFlags(flags);
        }
        if (photoOutput != null) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoOutput);
        }
        return intent;
    }
}
