package iam.thevoid.mediapicker.builder;

import android.content.Intent;

/**
 * Created by iam on 03.04.17.
 */

public class ImageIntentBuilder {

    private int flags = 0;
    private Mimetype mimetype = Mimetype.BOTH_IMAGE_AND_VIDEO;
    private boolean localOnly = false;

    public ImageIntentBuilder setLocalOnly(boolean localOnly) {
        this.localOnly = localOnly;
        return this;
    }

    public ImageIntentBuilder setMimetype(Mimetype mimetype) {
        this.mimetype = mimetype;
        return this;
    }

    public Intent build() {
        Intent intent = new Intent(Intent.ACTION_PICK);

        intent.setType(mimetype.getType());

        intent.setAction(Intent.ACTION_GET_CONTENT);

        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, localOnly);

        if (flags != 0) {
            intent.setFlags(flags);
        }
        return intent;
    }

    public enum Mimetype {
        IMAGE("image/*"),
        VIDEO("video/*"),
        BOTH_IMAGE_AND_VIDEO("image/*, video/*");

        Mimetype(String type) {
            this.type = type;
        }

        private String type;

        public String getType() {
            return type;
        }
    }
}
