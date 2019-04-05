package iam.thevoid.mediapickertest;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;

import iam.thevoid.mediapicker.builder.VideoIntentBuilder;
import iam.thevoid.mediapicker.cropper.CropArea;
import iam.thevoid.mediapicker.rxmediapicker.Purpose;
import iam.thevoid.mediapicker.rxmediapicker.RxMediaPicker;
import iam.thevoid.mediapicker.rxmediapicker.UriTransformer;
import iam.thevoid.mediapicker.rxmediapicker.metrics.Quality;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    ImageView mImageView;
    Button mIndeterminateBtn;
    Button mJustBtn;
    Button mCircleCropBtn;
    Button mToastFileSizeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageView = (ImageView) findViewById(R.id.image);
        mIndeterminateBtn = (Button) findViewById(R.id.btn_indeterminate);
        mIndeterminateBtn.setOnClickListener(this);
        mToastFileSizeBtn = (Button) findViewById(R.id.btn_toast_filesize);
        mToastFileSizeBtn.setOnClickListener(this);
        mJustBtn = (Button) findViewById(R.id.btn_just);
        mJustBtn.setOnClickListener(this);
        mCircleCropBtn = (Button) findViewById(R.id.btn_round_crop);
        mCircleCropBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_indeterminate:
                RxMediaPicker.builder(this)
                        .pick(Purpose.Pick.VIDEO)
                        .take(Purpose.Take.VIDEO)
                        .videoQuality(new Quality(VideoIntentBuilder.VideoQuality.HIGH))
                        .build()
                        .subscribe(this::loadIage);
                break;
            case R.id.btn_just:
                RxMediaPicker.builder(this)
                        .pick(Purpose.Pick.IMAGE)
                        .take(Purpose.Take.PHOTO)
                        .build()
                        .subscribe(this::loadIage);
                break;
            case R.id.btn_round_crop:
                RxMediaPicker.builder(this)
                        .crop(CropArea.circle())
                        .pick(Purpose.Pick.IMAGE)
                        .take(Purpose.Take.PHOTO)
                        .build()
                        .subscribe(this::loadIage);

                break;
            case R.id.btn_toast_filesize:
                RxMediaPicker.builder(this)
                        .pick(Purpose.Pick.IMAGE)
                        .take(Purpose.Take.PHOTO)
                        .build()
                        .compose(UriTransformer.file(this))
                        .subscribe(file -> {
                            Toast.makeText(this, "File size is " + filesizeInMb(file), Toast.LENGTH_LONG).show();
                        });

                break;
        }
    }

    private String filesizeInMb(File file) {
        double size = file.length();
        return "" + size / 1024 / 1024;
    }

    private void loadIage(Uri filepath) {
        Glide
            .with(this)
            .asBitmap()
            .load(filepath)
            .into(mImageView);
    }
}
