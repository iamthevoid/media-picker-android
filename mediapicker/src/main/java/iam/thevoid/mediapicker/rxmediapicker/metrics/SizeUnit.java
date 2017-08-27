package iam.thevoid.mediapicker.rxmediapicker.metrics;

/**
 * Created by iam on 03.04.17.
 */

public enum SizeUnit {
    BYTE(1),
    KILOBYTE(1024 * BYTE.getBytes()),
    MEGABYTE(1024 * KILOBYTE.getBytes()),
    GIGABYTE(1024 * MEGABYTE.getBytes());

    long bytes;

    SizeUnit(long bytes) {
        this.bytes = bytes;
    }

    public long getBytes() {
        return bytes;
    }

    public long bits() {
        return 8 * bytes;
    }
}
