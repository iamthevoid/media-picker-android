package iam.thevoid.mediapicker.rxmediapicker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.List;

import iam.thevoid.mediapicker.R;
import iam.thevoid.mediapicker.builder.ImageIntentBuilder;
import iam.thevoid.mediapicker.builder.PhotoIntentBuilder;
import iam.thevoid.mediapicker.builder.VideoIntentBuilder;
import iam.thevoid.mediapicker.chooser.IntentData;
import iam.thevoid.mediapicker.rxmediapicker.metrics.SizeUnit;

/**
 * Created by iam on 14/08/2017.
 */

public interface Purpose {

    int REQUEST_PICK_GALLERY = 0x999;
    int REQUEST_TAKE_PHOTO = 0x888;
    int REQUEST_TAKE_VIDEO = 0x777;
    int REQUEST_PICK_IMAGE = 0x666;
    int REQUEST_PICK_VIDEO = 0x555;

    enum Take implements Purpose {
        PHOTO {
            @Override
            public int requestCode() {
                return REQUEST_TAKE_PHOTO;
            }

            @Override
            public Intent getIntent(Context context, Bundle data) {
                return new PhotoIntentBuilder()
                        .build(context);
            }

            @Override
            public IntentData getIntentData(Context context, Bundle bundle) {
                return new IntentData(getIntent(context, bundle), requestCode(), R.string.take_photo);
            }
        },

        VIDEO {
            @Override
            public Intent getIntent(Context context, Bundle data) {
                return new VideoIntentBuilder()
                        .setVideoDuration(data.getLong(RxMediaPicker.EXTRA_VIDEO_MAX_DURATION, 0))
                        .setVideoFileSize(data.getLong(RxMediaPicker.EXTRA_VIDEO_MAX_SIZE), SizeUnit.BYTE)
                        .setVideoQuality(VideoIntentBuilder.VideoQuality.HIGH)
                        .build();
            }

            @Override
            public int requestCode() {
                return REQUEST_TAKE_VIDEO;
            }

            @Override
            public IntentData getIntentData(Context context, Bundle bundle) {
                return new IntentData(getIntent(context, bundle), requestCode(), R.string.take_video);
            }
        }
    }

    enum Pick implements Purpose {
        IMAGE {
            @Override
            public Intent getIntent(Context context, Bundle data) {
                return new ImageIntentBuilder()
                        .setLocalOnly(false)
                        .setMimetype(ImageIntentBuilder.Mimetype.IMAGE)
                        .build();
            }

            @Override
            public int requestCode() {
                return REQUEST_PICK_IMAGE;
            }
        },

        VIDEO {
            @Override
            public Intent getIntent(Context context, Bundle data) {
                return new ImageIntentBuilder()
                        .setLocalOnly(false)
                        .setMimetype(ImageIntentBuilder.Mimetype.VIDEO)
                        .build();
            }

            @Override
            public int requestCode() {
                return REQUEST_PICK_VIDEO;
            }
        }
    }

    enum Hidden implements Purpose {

        GALLERY {
            @Override
            public int requestCode() {
                return REQUEST_PICK_GALLERY;
            }

            @Override
            public Intent getIntent(Context context, Bundle data) {
                return new ImageIntentBuilder()
                        .setLocalOnly(false)
                        .setMimetype(ImageIntentBuilder.Mimetype.BOTH_IMAGE_AND_VIDEO)
                        .build();
            }
        }
    }

    int requestCode();

    Intent getIntent(Context context, Bundle data);

    default IntentData getIntentData(Context context, Bundle bundle) {
        return new IntentData(getIntent(context, bundle), requestCode());
    }

    static boolean contains(List<Purpose> purposes, Purpose purpose) {
        return purposes.contains(purpose);
    }
}