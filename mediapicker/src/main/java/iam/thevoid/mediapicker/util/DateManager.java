package iam.thevoid.mediapicker.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by iam on 11.04.17.
 */

public class DateManager {

    public static long getTime() {
        return new Date().getTime();
    }

    public static String formatDateToString(String photoDatePattern, Date date) {
        return new SimpleDateFormat(photoDatePattern).format(date);
    }
}
