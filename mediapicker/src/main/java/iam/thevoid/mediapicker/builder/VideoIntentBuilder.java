package iam.thevoid.mediapicker.builder;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import iam.thevoid.mediapicker.rxmediapicker.metrics.SizeUnit;

public class VideoIntentBuilder {

    private long videoDuration = -1;
    private long videoQuality = -1;
    private long videoFileSize = -1;
    private Uri videoOutput;
    private int flags = 0;

    public VideoIntentBuilder setVideoDuration(long videoDuration) {
        this.videoDuration = videoDuration;
        return this;
    }

    public VideoIntentBuilder setVideoFileSize(long videoFileSize, @NonNull SizeUnit unit) {
        this.videoFileSize = videoFileSize * unit.getBytes();
        return this;
    }

    public VideoIntentBuilder setVideoQuality(VideoQuality videoQuality) {
        this.videoQuality = videoQuality.getQuality();
        return this;
    }

    public VideoIntentBuilder setFlags(int flags) {
        this.flags = flags;
        return this;
    }

    public Intent build() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (videoDuration != -1) {
            intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, videoDuration);
        }
        if (videoQuality != -1) {
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, videoQuality);
        }
        if (videoFileSize != -1) {
            intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, videoFileSize);
        }
        if (flags != 0) {
            intent.setFlags(flags);
        }
//        if (videoOutput != null) {
//            intent.putExtra(MediaStore.EXTRA_OUTPUT, videoOutput);
//        }
        return intent;
    }

    public enum VideoQuality {
        STANDART(0),
        HIGH(1);

        VideoQuality(int quality) {
            this.quality = quality;
        }

        private int quality;

        public int getQuality() {
            return quality;
        }
    }
}
