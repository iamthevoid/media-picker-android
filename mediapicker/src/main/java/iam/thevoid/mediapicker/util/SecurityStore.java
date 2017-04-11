package iam.thevoid.mediapicker.util;

import com.orhanobut.hawk.Hawk;

/**
 * Created by iam on 11.04.17.
 */

public class SecurityStore {

    private static final String PHOTO_PATH = "mediapicker.com.iam_PHOTO_PATH";

    public static void setMediaPickerPhotoPath(String photoPath) {
        Hawk.put(PHOTO_PATH, photoPath);
    }

    public static String getMediaPickerPhotoPath() {
        return Hawk.get(PHOTO_PATH);
    }

    public static void deleteMediaPickerPhotoPath() {
        Hawk.delete(PHOTO_PATH);
    }
}
