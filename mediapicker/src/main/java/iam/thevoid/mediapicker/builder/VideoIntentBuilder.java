package iam.thevoid.mediapicker.builder;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;

import java.io.File;

import iam.thevoid.mediapicker.util.SizeUnit;


/**
 * Created by iam on 03.04.17.
 */

public class VideoIntentBuilder {

    private int videoDuration = -1;
    private int videoQuality = -1;
    private long videoFileSize = -1;
    private int flags = 0;

    public VideoIntentBuilder setVideoDuration(int videoDuration) {
        this.videoDuration = videoDuration;
        return this;
    }

    public VideoIntentBuilder setVideoFileSize(int videoFileSize, @NonNull SizeUnit unit) {
        this.videoFileSize = (long) videoFileSize * unit.getBytes();
        return this;
    }

    public VideoIntentBuilder setVideoQuality(VideoQuality videoQuality) {
        this.videoQuality = videoQuality.getQuality();
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
