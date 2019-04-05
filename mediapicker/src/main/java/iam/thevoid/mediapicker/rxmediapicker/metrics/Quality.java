package iam.thevoid.mediapicker.rxmediapicker.metrics;

import iam.thevoid.mediapicker.builder.VideoIntentBuilder;

public class Quality {
    private int quality;

    public Quality(VideoIntentBuilder.VideoQuality quality) {
        this.quality = quality.getQuality();
    }

    public int getQuality() {
        return quality;
    }
}
