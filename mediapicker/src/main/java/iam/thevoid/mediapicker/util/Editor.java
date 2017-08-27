package iam.thevoid.mediapicker.util;

import java.util.Date;

/**
 * Created by iam on 22/08/2017.
 */

public final class Editor {
    private Editor() {
    }

    public static String currentDateFilename(String prefix, String ext) {
        if (ext == null) {
            ext = "";
        }

        if (prefix == null) {
            prefix = "";
        }

        return String.format("%s%s%s",
                (prefix.length() == 0 ? "" : prefix + "_"),
                DateManager.formatDateToString(
                        "yyyy_MM_dd_HH_mm_ss", new Date(DateManager.getTime())),
                ext
        );
    }

}
