package iam.thevoid.mediapicker.builder;

import android.content.Intent;
import android.provider.MediaStore;

/**
 * Created by iam on 03.04.17.
 */

public class PhotoIntentBuilder {

    private static final String TAG = PhotoIntentBuilder.class.getSimpleName();

    private int flags = 0;


    public PhotoIntentBuilder setFlags(int flags) {
        this.flags = flags;
        return this;
    }

    public Intent build() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (flags != 0) {
            intent.setFlags(flags);
        }

        return intent;
    }
}
