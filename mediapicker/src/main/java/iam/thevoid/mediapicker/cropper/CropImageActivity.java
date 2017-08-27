package iam.thevoid.mediapicker.cropper;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;

import iam.thevoid.mediapicker.R;

/**
 * Created by iam on 12/07/2017.
 */

public class CropImageActivity extends AppCompatActivity implements CropImageView.OnSetImageUriCompleteListener, CropImageView.OnCropImageCompleteListener {

    public static final String EXTRA_SOURCE = "CROP_IMAGE_EXTRA_SOURCE";
    public static final String EXTRA_OPTIONS = "CROP_IMAGE_EXTRA_OPTIONS";

    private CropImageView mCropImageView;
    private Uri mCropImageUri;
    private CropImageOptions mOptions;

    public CropImageActivity() {
    }

    @SuppressLint({"NewApi"})
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.crop_image_activity);
        this.mCropImageView = (CropImageView) this.findViewById(R.id.cropImageView);
        Intent intent = this.getIntent();
        this.mCropImageUri = intent.getParcelableExtra(EXTRA_SOURCE);
        this.mOptions = intent.getParcelableExtra(EXTRA_OPTIONS);
        if (savedInstanceState == null) {
            if (this.mCropImageUri != null && !this.mCropImageUri.equals(Uri.EMPTY)) {
                if (CropImage.isReadExternalStoragePermissionsRequired(this, this.mCropImageUri)) {
                    this.requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE"}, 201);
                } else {
                    this.mCropImageView.setImageUriAsync(this.mCropImageUri);
                }
            } else if (CropImage.isExplicitCameraPermissionRequired(this)) {
                this.requestPermissions(new String[]{"android.permission.CAMERA"}, 2011);
            } else {
                CropImage.startPickImageActivity(this);
            }
        } else {
            this.mOptions = savedInstanceState.getParcelable(EXTRA_OPTIONS);
            this.mCropImageUri = savedInstanceState.getParcelable(EXTRA_SOURCE);
            if (this.mCropImageUri != null && !this.mCropImageUri.equals(Uri.EMPTY)) {
                if (CropImage.isReadExternalStoragePermissionsRequired(this, this.mCropImageUri)) {
                    this.requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE"}, 201);
                } else {
                    this.mCropImageView.setImageUriAsync(this.mCropImageUri);
                }
            } else if (CropImage.isExplicitCameraPermissionRequired(this)) {
                this.requestPermissions(new String[]{"android.permission.CAMERA"}, 2011);
            } else {
                CropImage.startPickImageActivity(this);
            }
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            String title = this.mOptions.activityTitle != null && !this.mOptions.activityTitle.isEmpty() ? this.mOptions.activityTitle : this.getResources().getString(R.string.crop_image_activity_title);
            final Drawable upArrow = getResources().getDrawable(R.drawable.ic_back);
            upArrow.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

            Spannable text = new SpannableString(actionBar.getTitle());
            text.setSpan(new ForegroundColorSpan(Color.WHITE), 0, text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            actionBar.setTitle(text);

            actionBar.setHomeAsUpIndicator(upArrow);
            actionBar.setTitle(title);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EXTRA_SOURCE, mCropImageUri);
        outState.putParcelable(EXTRA_OPTIONS, mOptions);
    }

    protected void onStart() {
        super.onStart();
        this.mCropImageView.setOnSetImageUriCompleteListener(this);
        this.mCropImageView.setOnCropImageCompleteListener(this);
    }

    protected void onStop() {
        super.onStop();
        this.mCropImageView.setOnSetImageUriCompleteListener(null);
        this.mCropImageView.setOnCropImageCompleteListener(null);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.crop_image_menu, menu);
        if (!this.mOptions.allowRotation) {
            menu.removeItem(R.id.crop_image_menu_rotate_left);
            menu.removeItem(R.id.crop_image_menu_rotate_right);
        } else if (this.mOptions.allowCounterRotation) {
            menu.findItem(R.id.crop_image_menu_rotate_left).setVisible(true);
        }

        if (!this.mOptions.allowFlipping) {
            menu.removeItem(R.id.crop_image_menu_flip);
        }

//        Drawable cropIcon = null;
//
//        try {
//            cropIcon = ContextCompat.getDrawable(this, R.drawable.crop_image_menu_crop);
//            if (cropIcon != null) {
//                menu.findItem(R.id.crop_image_menu_crop).setIcon(cropIcon);
//            }
//        } catch (Exception var4) {
//            ;
//        }

//        if (this.mOptions.activityMenuIconColor != 0) {
//            this.updateMenuItemIconColor(menu, R.id.crop_image_menu_rotate_left, this.mOptions.activityMenuIconColor);
//            this.updateMenuItemIconColor(menu, R.id.crop_image_menu_rotate_right, this.mOptions.activityMenuIconColor);
//            this.updateMenuItemIconColor(menu, R.id.crop_image_menu_flip, this.mOptions.activityMenuIconColor);
//            if (cropIcon != null) {
//                this.updateMenuItemIconColor(menu, R.id.crop_image_menu_crop, this.mOptions.activityMenuIconColor);
//            }
//        }

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.crop_image_menu_crop) {
            this.cropImage();
            return true;
        } else if (item.getItemId() == R.id.crop_image_menu_rotate_left) {
            this.rotateImage(-this.mOptions.rotationDegrees);
            return true;
        } else if (item.getItemId() == R.id.crop_image_menu_rotate_right) {
            this.rotateImage(this.mOptions.rotationDegrees);
            return true;
        } else if (item.getItemId() == R.id.crop_image_menu_flip_horizontally) {
            this.mCropImageView.flipImageHorizontally();
            return true;
        } else if (item.getItemId() == R.id.crop_image_menu_flip_vertically) {
            this.mCropImageView.flipImageVertically();
            return true;
        } else if (item.getItemId() == 16908332) {
            this.setResultCancel();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public void onBackPressed() {
        super.onBackPressed();
        this.setResultCancel();
    }

    @SuppressLint({"NewApi"})
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 200) {
            if (resultCode == 0) {
                this.setResultCancel();
            }

            if (resultCode == -1) {
                this.mCropImageUri = CropImage.getPickImageResultUri(this, data);
                if (CropImage.isReadExternalStoragePermissionsRequired(this, this.mCropImageUri)) {
                    this.requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE"}, 201);
                } else {
                    this.mCropImageView.setImageUriAsync(this.mCropImageUri);
                }
            }
        }

    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 201) {
            if (this.mCropImageUri != null && grantResults.length > 0 && grantResults[0] == 0) {
                this.mCropImageView.setImageUriAsync(this.mCropImageUri);
            } else {
                Toast.makeText(this, "Cancelling, required permissions are not granted", 1).show();
                this.setResultCancel();
            }
        }

        if (requestCode == 2011) {
            CropImage.startPickImageActivity(this);
        }

    }

    public void onSetImageUriComplete(CropImageView view, Uri uri, Exception error) {
        if (error == null) {
            if (this.mOptions.initialCropWindowRectangle != null) {
                this.mCropImageView.setCropRect(this.mOptions.initialCropWindowRectangle);
            }

            if (this.mOptions.initialRotation > -1) {
                this.mCropImageView.setRotatedDegrees(this.mOptions.initialRotation);
            }
        } else {
            this.setResult((Uri) null, error, 1);
        }

    }

    public void onCropImageComplete(CropImageView view, CropImageView.CropResult result) {
        this.setResult(result.getUri(), result.getError(), result.getSampleSize());
    }

    protected void cropImage() {
        if (this.mOptions.noOutputImage) {
            this.setResult((Uri) null, (Exception) null, 1);
        } else {
            Uri outputUri = this.getOutputUri();
            this.mCropImageView.saveCroppedImageAsync(outputUri, this.mOptions.outputCompressFormat, this.mOptions.outputCompressQuality, this.mOptions.outputRequestWidth, this.mOptions.outputRequestHeight, this.mOptions.outputRequestSizeOptions);
        }

    }

    protected void rotateImage(int degrees) {
        this.mCropImageView.rotateImage(degrees);
    }

    protected Uri getOutputUri() {
        Uri outputUri = this.mOptions.outputUri;
        if (outputUri.equals(Uri.EMPTY)) {
            try {
                String ext = this.mOptions.outputCompressFormat == Bitmap.CompressFormat.JPEG ? ".jpg" : (this.mOptions.outputCompressFormat == Bitmap.CompressFormat.PNG ? ".png" : ".webp");
                outputUri = Uri.fromFile(File.createTempFile("cropped", ext, this.getCacheDir()));
            } catch (IOException var3) {
                throw new RuntimeException("Failed to create temp file for output image", var3);
            }
        }

        return outputUri;
    }

    protected void setResult(Uri uri, Exception error, int sampleSize) {
        int resultCode = error == null ? -1 : 204;
        this.setResult(resultCode, this.getResultIntent(uri, error, sampleSize));
        this.finish();
    }

    protected void setResultCancel() {
        this.setResult(0);
        this.finish();
    }

    protected Intent getResultIntent(Uri uri, Exception error, int sampleSize) {
        CropImage.ActivityResult result = new CropImage.ActivityResult(this.mCropImageView.getImageUri(), uri, error, this.mCropImageView.getCropPoints(), this.mCropImageView.getCropRect(), this.mCropImageView.getRotatedDegrees(), sampleSize);
        Intent intent = new Intent();
        intent.putExtra("CROP_IMAGE_EXTRA_RESULT", result);
        return intent;
    }

    private void updateMenuItemIconColor(Menu menu, int itemId, int color) {
        MenuItem menuItem = menu.findItem(itemId);
        if (menuItem != null) {
            Drawable menuItemIcon = menuItem.getIcon();
            if (menuItemIcon != null) {
                try {
                    menuItemIcon.mutate();
                    menuItemIcon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                    menuItem.setIcon(menuItemIcon);
                } catch (Exception var7) {
                    ;
                }
            }
        }

    }
}
