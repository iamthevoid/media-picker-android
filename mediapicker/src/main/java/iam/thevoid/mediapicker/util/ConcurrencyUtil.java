package iam.thevoid.mediapicker.util;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by iam on 22/08/2017.
 */

public final class ConcurrencyUtil {

    private ConcurrencyUtil() {
    }

    public static void runInMainThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    public static void postDelayed(Runnable runnable, long l) {
        new Handler().postDelayed(runnable, 500);
    }
}
