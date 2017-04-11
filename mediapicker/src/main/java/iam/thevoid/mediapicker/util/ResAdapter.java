package iam.thevoid.mediapicker.util;

import android.content.Context;
import android.support.annotation.StringRes;

/**
 * Created by iam on 11.04.17.
 */

public class ResAdapter {

    public static String getString(Context context, @StringRes int resource) {
        if (resource <= 0) {
            return null;
        }

        return context.getString(resource);
    }

}
