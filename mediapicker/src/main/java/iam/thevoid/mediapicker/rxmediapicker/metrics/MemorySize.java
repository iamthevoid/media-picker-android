package iam.thevoid.mediapicker.rxmediapicker.metrics;

public class MemorySize {
    private long size;
    private SizeUnit unit;

    public MemorySize(int size, SizeUnit unit) {

        if (size < 0) {
            this.size = 0;
        }

        this.size = size;
        this.unit = unit;
    }

    public long getBytes() {
        return size * unit.getBytes();
    }
}
