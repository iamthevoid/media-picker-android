package iam.thevoid.mediapicker.rxmediapicker.metrics;

import java.util.concurrent.TimeUnit;

public class Duration {
    private TimeUnit timeUnit;
    private long duration;

    public Duration(long duration, TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
        this.duration = duration;
    }

    public long getSeconds() {
        return timeUnit.toSeconds(duration);
    }
}