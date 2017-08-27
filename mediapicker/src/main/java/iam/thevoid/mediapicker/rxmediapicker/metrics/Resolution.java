package iam.thevoid.mediapicker.rxmediapicker.metrics;

/**
 * Created by iam on 14/08/2017.
 */

public class Resolution {
    private long width;
    private long height;

    public Resolution(long width, long height) {
        this.width = width;
        this.height = height;
    }

    public long getWidth() {
        return width;
    }

    public long getHeight() {
        return height;
    }
}
