package iam.thevoid.mediapicker.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by iam on 22/08/2017.
 */

public final class DateManager {
    private DateManager() {}

    public static long getTime() {
        return System.currentTimeMillis();
    }

    public static String formatDateToString(String outputPattern, Date sDate) {
        return new SimpleDateFormat(outputPattern, Locale.getDefault()).format(sDate);
    }
}
