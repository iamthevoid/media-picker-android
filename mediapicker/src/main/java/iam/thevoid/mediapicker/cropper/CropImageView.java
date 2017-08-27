package iam.thevoid.mediapicker.cropper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.media.ExifInterface;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.lang.ref.WeakReference;
import java.util.UUID;

import iam.thevoid.mediapicker.R;

/**
 * Created by iam on 12/07/2017.
 */

public class CropImageView extends FrameLayout {
    private final ImageView mImageView;
    private final CropOverlayView mCropOverlayView;
    private final Matrix mImageMatrix;
    private final Matrix mImageInverseMatrix;
    private final ProgressBar mProgressBar;
    private final float[] mImagePoints;
    private CropImageAnimation mAnimation;
    private Bitmap mBitmap;
    private int mInitialDegreesRotated;
    private int mDegreesRotated;
    private boolean mFlipHorizontally;
    private boolean mFlipVertically;
    private int mLayoutWidth;
    private int mLayoutHeight;
    private int mImageResource;
    private ScaleType mScaleType;
    private boolean mSaveBitmapToInstanceState;
    private boolean mShowCropOverlay;
    private boolean mShowProgressBar;
    private boolean mAutoZoomEnabled;
    private int mMaxZoom;
    private OnSetCropOverlayReleasedListener mOnCropOverlayReleasedListener;
    private OnSetImageUriCompleteListener mOnSetImageUriCompleteListener;
    private OnCropImageCompleteListener mOnCropImageCompleteListener;
    private Uri mLoadedImageUri;
    private int mLoadedSampleSize;
    private float mZoom;
    private float mZoomOffsetX;
    private float mZoomOffsetY;
    private RectF mRestoreCropWindowRect;
    private int mRestoreDegreesRotated;
    private boolean mSizeChanged;
    private Uri mSaveInstanceStateBitmapUri;
    private WeakReference<BitmapLoadingWorkerTask> mBitmapLoadingWorkerTask;
    private WeakReference<BitmapCroppingWorkerTask> mBitmapCroppingWorkerTask;

    public CropImageView(Context context) {
        this(context, (AttributeSet) null);
    }

    public CropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mImageMatrix = new Matrix();
        this.mImageInverseMatrix = new Matrix();
        this.mImagePoints = new float[8];
        this.mSaveBitmapToInstanceState = false;
        this.mShowCropOverlay = true;
        this.mShowProgressBar = true;
        this.mAutoZoomEnabled = true;
        this.mLoadedSampleSize = 1;
        this.mZoom = 1.0F;
        CropImageOptions options = null;
        Intent intent = context instanceof Activity ? ((Activity) context).getIntent() : null;
        if (intent != null) {
            options = (CropImageOptions) intent.getParcelableExtra("CROP_IMAGE_EXTRA_OPTIONS");
        }

        if (options == null) {
            options = new CropImageOptions();
            if (attrs != null) {
                TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CropImageView, 0, 0);

                try {
                    options.fixAspectRatio = ta.getBoolean(R.styleable.CropImageView_cropFixAspectRatio, options.fixAspectRatio);
                    options.aspectRatioX = ta.getInteger(R.styleable.CropImageView_cropAspectRatioX, options.aspectRatioX);
                    options.aspectRatioY = ta.getInteger(R.styleable.CropImageView_cropAspectRatioY, options.aspectRatioY);
                    options.scaleType = ScaleType.values()[ta.getInt(R.styleable.CropImageView_cropScaleType, options.scaleType.ordinal())];
                    options.autoZoomEnabled = ta.getBoolean(R.styleable.CropImageView_cropAutoZoomEnabled, options.autoZoomEnabled);
                    options.multiTouchEnabled = ta.getBoolean(R.styleable.CropImageView_cropMultiTouchEnabled, options.multiTouchEnabled);
                    options.maxZoom = ta.getInteger(R.styleable.CropImageView_cropMaxZoom, options.maxZoom);
                    options.cropShape = CropShape.values()[ta.getInt(R.styleable.CropImageView_cropShape, options.cropShape.ordinal())];
                    options.guidelines = Guidelines.values()[ta.getInt(R.styleable.CropImageView_cropGuidelines, options.guidelines.ordinal())];
                    options.snapRadius = ta.getDimension(R.styleable.CropImageView_cropSnapRadius, options.snapRadius);
                    options.touchRadius = ta.getDimension(R.styleable.CropImageView_cropTouchRadius, options.touchRadius);
                    options.initialCropWindowPaddingRatio = ta.getFloat(R.styleable.CropImageView_cropInitialCropWindowPaddingRatio, options.initialCropWindowPaddingRatio);
                    options.borderLineThickness = ta.getDimension(R.styleable.CropImageView_cropBorderLineThickness, options.borderLineThickness);
                    options.borderLineColor = ta.getInteger(R.styleable.CropImageView_cropBorderLineColor, options.borderLineColor);
                    options.borderCornerThickness = ta.getDimension(R.styleable.CropImageView_cropBorderCornerThickness, options.borderCornerThickness);
                    options.borderCornerOffset = ta.getDimension(R.styleable.CropImageView_cropBorderCornerOffset, options.borderCornerOffset);
                    options.borderCornerLength = ta.getDimension(R.styleable.CropImageView_cropBorderCornerLength, options.borderCornerLength);
                    options.borderCornerColor = ta.getInteger(R.styleable.CropImageView_cropBorderCornerColor, options.borderCornerColor);
                    options.guidelinesThickness = ta.getDimension(R.styleable.CropImageView_cropGuidelinesThickness, options.guidelinesThickness);
                    options.guidelinesColor = ta.getInteger(R.styleable.CropImageView_cropGuidelinesColor, options.guidelinesColor);
                    options.backgroundColor = ta.getInteger(R.styleable.CropImageView_cropBackgroundColor, options.backgroundColor);
                    options.showCropOverlay = ta.getBoolean(R.styleable.CropImageView_cropShowCropOverlay, this.mShowCropOverlay);
                    options.showProgressBar = ta.getBoolean(R.styleable.CropImageView_cropShowProgressBar, this.mShowProgressBar);
                    options.borderCornerThickness = ta.getDimension(R.styleable.CropImageView_cropBorderCornerThickness, options.borderCornerThickness);
                    options.minCropWindowWidth = (int) ta.getDimension(R.styleable.CropImageView_cropMinCropWindowWidth, (float) options.minCropWindowWidth);
                    options.minCropWindowHeight = (int) ta.getDimension(R.styleable.CropImageView_cropMinCropWindowHeight, (float) options.minCropWindowHeight);
                    options.minCropResultWidth = (int) ta.getFloat(R.styleable.CropImageView_cropMinCropResultWidthPX, (float) options.minCropResultWidth);
                    options.minCropResultHeight = (int) ta.getFloat(R.styleable.CropImageView_cropMinCropResultHeightPX, (float) options.minCropResultHeight);
                    options.maxCropResultWidth = (int) ta.getFloat(R.styleable.CropImageView_cropMaxCropResultWidthPX, (float) options.maxCropResultWidth);
                    options.maxCropResultHeight = (int) ta.getFloat(R.styleable.CropImageView_cropMaxCropResultHeightPX, (float) options.maxCropResultHeight);
                    options.flipHorizontally = ta.getBoolean(R.styleable.CropImageView_cropFlipHorizontally, options.flipHorizontally);
                    options.flipVertically = ta.getBoolean(R.styleable.CropImageView_cropFlipHorizontally, options.flipVertically);
                    this.mSaveBitmapToInstanceState = ta.getBoolean(R.styleable.CropImageView_cropSaveBitmapToInstanceState, this.mSaveBitmapToInstanceState);
                    if (ta.hasValue(R.styleable.CropImageView_cropAspectRatioX) && ta.hasValue(R.styleable.CropImageView_cropAspectRatioX) && !ta.hasValue(R.styleable.CropImageView_cropFixAspectRatio)) {
                        options.fixAspectRatio = true;
                    }
                } finally {
                    ta.recycle();
                }
            }
        }

        options.validate();
        this.mScaleType = options.scaleType;
        this.mAutoZoomEnabled = options.autoZoomEnabled;
        this.mMaxZoom = options.maxZoom;
        this.mShowCropOverlay = options.showCropOverlay;
        this.mShowProgressBar = options.showProgressBar;
        this.mFlipHorizontally = options.flipHorizontally;
        this.mFlipVertically = options.flipVertically;
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.crop_image_view, this, true);
        this.mImageView = (ImageView) v.findViewById(R.id.ImageView_image);
        this.mImageView.setScaleType(ImageView.ScaleType.MATRIX);
        this.mCropOverlayView = (CropOverlayView) v.findViewById(R.id.CropOverlayView);
        this.mCropOverlayView.setCropWindowChangeListener(new CropOverlayView.CropWindowChangeListener() {
            public void onCropWindowChanged(boolean inProgress) {
                CropImageView.this.handleCropWindowChanged(inProgress, true);
                OnSetCropOverlayReleasedListener listener = CropImageView.this.mOnCropOverlayReleasedListener;
                if (listener != null && !inProgress) {
                    listener.onCropOverlayReleased(CropImageView.this.getCropRect());
                }

            }
        });
        this.mCropOverlayView.setInitialAttributeValues(options);
        this.mProgressBar = (ProgressBar) v.findViewById(R.id.CropProgressBar);
        this.setProgressBarVisibility();
    }

    public ScaleType getScaleType() {
        return this.mScaleType;
    }

    public void setScaleType(ScaleType scaleType) {
        if (scaleType != this.mScaleType) {
            this.mScaleType = scaleType;
            this.mZoom = 1.0F;
            this.mZoomOffsetX = this.mZoomOffsetY = 0.0F;
            this.mCropOverlayView.resetCropOverlayView();
            this.requestLayout();
        }

    }

    public CropShape getCropShape() {
        return this.mCropOverlayView.getCropShape();
    }

    public void setCropShape(CropShape cropShape) {
        this.mCropOverlayView.setCropShape(cropShape);
    }

    public boolean isAutoZoomEnabled() {
        return this.mAutoZoomEnabled;
    }

    public void setAutoZoomEnabled(boolean autoZoomEnabled) {
        if (this.mAutoZoomEnabled != autoZoomEnabled) {
            this.mAutoZoomEnabled = autoZoomEnabled;
            this.handleCropWindowChanged(false, false);
            this.mCropOverlayView.invalidate();
        }

    }

    public void setMultiTouchEnabled(boolean multiTouchEnabled) {
        if (this.mCropOverlayView.setMultiTouchEnabled(multiTouchEnabled)) {
            this.handleCropWindowChanged(false, false);
            this.mCropOverlayView.invalidate();
        }

    }

    public int getMaxZoom() {
        return this.mMaxZoom;
    }

    public void setMaxZoom(int maxZoom) {
        if (this.mMaxZoom != maxZoom && maxZoom > 0) {
            this.mMaxZoom = maxZoom;
            this.handleCropWindowChanged(false, false);
            this.mCropOverlayView.invalidate();
        }

    }

    public void setMinCropResultSize(int minCropResultWidth, int minCropResultHeight) {
        this.mCropOverlayView.setMinCropResultSize(minCropResultWidth, minCropResultHeight);
    }

    public void setMaxCropResultSize(int maxCropResultWidth, int maxCropResultHeight) {
        this.mCropOverlayView.setMaxCropResultSize(maxCropResultWidth, maxCropResultHeight);
    }

    public int getRotatedDegrees() {
        return this.mDegreesRotated;
    }

    public void setRotatedDegrees(int degrees) {
        if (this.mDegreesRotated != degrees) {
            this.rotateImage(degrees - this.mDegreesRotated);
        }

    }

    public boolean isFixAspectRatio() {
        return this.mCropOverlayView.isFixAspectRatio();
    }

    public void setFixedAspectRatio(boolean fixAspectRatio) {
        this.mCropOverlayView.setFixedAspectRatio(fixAspectRatio);
    }

    public boolean isFlippedHorizontally() {
        return this.mFlipHorizontally;
    }

    public void setFlippedHorizontally(boolean flipHorizontally) {
        if (this.mFlipHorizontally != flipHorizontally) {
            this.mFlipHorizontally = flipHorizontally;
            this.applyImageMatrix((float) this.getWidth(), (float) this.getHeight(), true, false);
        }

    }

    public boolean isFlippedVertically() {
        return this.mFlipVertically;
    }

    public void setFlippedVertically(boolean flipVertically) {
        if (this.mFlipVertically != flipVertically) {
            this.mFlipVertically = flipVertically;
            this.applyImageMatrix((float) this.getWidth(), (float) this.getHeight(), true, false);
        }

    }

    public Guidelines getGuidelines() {
        return this.mCropOverlayView.getGuidelines();
    }

    public void setGuidelines(Guidelines guidelines) {
        this.mCropOverlayView.setGuidelines(guidelines);
    }

    public Pair<Integer, Integer> getAspectRatio() {
        return new Pair(Integer.valueOf(this.mCropOverlayView.getAspectRatioX()), Integer.valueOf(this.mCropOverlayView.getAspectRatioY()));
    }

    public void setAspectRatio(int aspectRatioX, int aspectRatioY) {
        this.mCropOverlayView.setAspectRatioX(aspectRatioX);
        this.mCropOverlayView.setAspectRatioY(aspectRatioY);
        this.setFixedAspectRatio(true);
    }

    public void clearAspectRatio() {
        this.mCropOverlayView.setAspectRatioX(1);
        this.mCropOverlayView.setAspectRatioY(1);
        this.setFixedAspectRatio(false);
    }

    public void setSnapRadius(float snapRadius) {
        if (snapRadius >= 0.0F) {
            this.mCropOverlayView.setSnapRadius(snapRadius);
        }

    }

    public boolean isShowProgressBar() {
        return this.mShowProgressBar;
    }

    public void setShowProgressBar(boolean showProgressBar) {
        if (this.mShowProgressBar != showProgressBar) {
            this.mShowProgressBar = showProgressBar;
            this.setProgressBarVisibility();
        }

    }

    public boolean isShowCropOverlay() {
        return this.mShowCropOverlay;
    }

    public void setShowCropOverlay(boolean showCropOverlay) {
        if (this.mShowCropOverlay != showCropOverlay) {
            this.mShowCropOverlay = showCropOverlay;
            this.setCropOverlayVisibility();
        }

    }

    public boolean isSaveBitmapToInstanceState() {
        return this.mSaveBitmapToInstanceState;
    }

    public void setSaveBitmapToInstanceState(boolean saveBitmapToInstanceState) {
        this.mSaveBitmapToInstanceState = saveBitmapToInstanceState;
    }

    public int getImageResource() {
        return this.mImageResource;
    }

    public Uri getImageUri() {
        return this.mLoadedImageUri;
    }

    public Rect getCropRect() {
        if (this.mBitmap != null) {
            float[] points = this.getCropPoints();
            int orgWidth = this.mBitmap.getWidth() * this.mLoadedSampleSize;
            int orgHeight = this.mBitmap.getHeight() * this.mLoadedSampleSize;
            return BitmapUtils.getRectFromPoints(points, orgWidth, orgHeight, this.mCropOverlayView.isFixAspectRatio(), this.mCropOverlayView.getAspectRatioX(), this.mCropOverlayView.getAspectRatioY());
        } else {
            return null;
        }
    }

    public float[] getCropPoints() {
        RectF cropWindowRect = this.mCropOverlayView.getCropWindowRect();
        float[] points = new float[]{cropWindowRect.left, cropWindowRect.top, cropWindowRect.right, cropWindowRect.top, cropWindowRect.right, cropWindowRect.bottom, cropWindowRect.left, cropWindowRect.bottom};
        this.mImageMatrix.invert(this.mImageInverseMatrix);
        this.mImageInverseMatrix.mapPoints(points);

        for (int i = 0; i < points.length; ++i) {
            points[i] *= (float) this.mLoadedSampleSize;
        }

        return points;
    }

    public void setCropRect(Rect rect) {
        this.mCropOverlayView.setInitialCropWindowRect(rect);
    }

    public void resetCropRect() {
        this.mZoom = 1.0F;
        this.mZoomOffsetX = 0.0F;
        this.mZoomOffsetY = 0.0F;
        this.mDegreesRotated = this.mInitialDegreesRotated;
        this.mFlipHorizontally = false;
        this.mFlipVertically = false;
        this.applyImageMatrix((float) this.getWidth(), (float) this.getHeight(), false, false);
        this.mCropOverlayView.resetCropWindowRect();
    }

    public Bitmap getCroppedImage() {
        return this.getCroppedImage(0, 0, RequestSizeOptions.NONE);
    }

    public Bitmap getCroppedImage(int reqWidth, int reqHeight) {
        return this.getCroppedImage(reqWidth, reqHeight, RequestSizeOptions.RESIZE_INSIDE);
    }

    public Bitmap getCroppedImage(int reqWidth, int reqHeight, RequestSizeOptions options) {
        Bitmap croppedBitmap = null;
        if (this.mBitmap != null) {
            this.mImageView.clearAnimation();
            reqWidth = options != RequestSizeOptions.NONE ? reqWidth : 0;
            reqHeight = options != RequestSizeOptions.NONE ? reqHeight : 0;
            if (this.mLoadedImageUri == null || this.mLoadedSampleSize <= 1 && options != RequestSizeOptions.SAMPLING) {
                croppedBitmap = BitmapUtils.cropBitmapObjectHandleOOM(this.mBitmap, this.getCropPoints(), this.mDegreesRotated, this.mCropOverlayView.isFixAspectRatio(), this.mCropOverlayView.getAspectRatioX(), this.mCropOverlayView.getAspectRatioY(), this.mFlipHorizontally, this.mFlipVertically).bitmap;
            } else {
                int orgWidth = this.mBitmap.getWidth() * this.mLoadedSampleSize;
                int orgHeight = this.mBitmap.getHeight() * this.mLoadedSampleSize;
                BitmapUtils.BitmapSampled bitmapSampled = BitmapUtils.cropBitmap(this.getContext(), this.mLoadedImageUri, this.getCropPoints(), this.mDegreesRotated, orgWidth, orgHeight, this.mCropOverlayView.isFixAspectRatio(), this.mCropOverlayView.getAspectRatioX(), this.mCropOverlayView.getAspectRatioY(), reqWidth, reqHeight, this.mFlipHorizontally, this.mFlipVertically);
                croppedBitmap = bitmapSampled.bitmap;
            }

            croppedBitmap = BitmapUtils.resizeBitmap(croppedBitmap, reqWidth, reqHeight, options);
        }

        return croppedBitmap;
    }

    public void getCroppedImageAsync() {
        this.getCroppedImageAsync(0, 0, RequestSizeOptions.NONE);
    }

    public void getCroppedImageAsync(int reqWidth, int reqHeight) {
        this.getCroppedImageAsync(reqWidth, reqHeight, RequestSizeOptions.RESIZE_INSIDE);
    }

    public void getCroppedImageAsync(int reqWidth, int reqHeight, RequestSizeOptions options) {
        if (this.mOnCropImageCompleteListener == null) {
            throw new IllegalArgumentException("mOnCropImageCompleteListener is not set");
        } else {
            this.startCropWorkerTask(reqWidth, reqHeight, options, (Uri) null, (Bitmap.CompressFormat) null, 0);
        }
    }

    public void saveCroppedImageAsync(Uri saveUri) {
        this.saveCroppedImageAsync(saveUri, Bitmap.CompressFormat.JPEG, 90, 0, 0, RequestSizeOptions.NONE);
    }

    public void saveCroppedImageAsync(Uri saveUri, Bitmap.CompressFormat saveCompressFormat, int saveCompressQuality) {
        this.saveCroppedImageAsync(saveUri, saveCompressFormat, saveCompressQuality, 0, 0, RequestSizeOptions.NONE);
    }

    public void saveCroppedImageAsync(Uri saveUri, Bitmap.CompressFormat saveCompressFormat, int saveCompressQuality, int reqWidth, int reqHeight) {
        this.saveCroppedImageAsync(saveUri, saveCompressFormat, saveCompressQuality, reqWidth, reqHeight, RequestSizeOptions.RESIZE_INSIDE);
    }

    public void saveCroppedImageAsync(Uri saveUri, Bitmap.CompressFormat saveCompressFormat, int saveCompressQuality, int reqWidth, int reqHeight, RequestSizeOptions options) {
        if (this.mOnCropImageCompleteListener == null) {
            throw new IllegalArgumentException("mOnCropImageCompleteListener is not set");
        } else {
            this.startCropWorkerTask(reqWidth, reqHeight, options, saveUri, saveCompressFormat, saveCompressQuality);
        }
    }

    public void setOnSetCropOverlayReleasedListener(OnSetCropOverlayReleasedListener listener) {
        this.mOnCropOverlayReleasedListener = listener;
    }

    public void setOnSetImageUriCompleteListener(OnSetImageUriCompleteListener listener) {
        this.mOnSetImageUriCompleteListener = listener;
    }

    public void setOnCropImageCompleteListener(OnCropImageCompleteListener listener) {
        this.mOnCropImageCompleteListener = listener;
    }

    public void setImageBitmap(Bitmap bitmap) {
        this.mCropOverlayView.setInitialCropWindowRect((Rect) null);
        this.setBitmap(bitmap, 0, (Uri) null, 1, 0);
    }

    public void setImageBitmap(Bitmap bitmap, ExifInterface exif) {
        int degreesRotated = 0;
        Bitmap setBitmap;
        if (bitmap != null && exif != null) {
            BitmapUtils.RotateBitmapResult result = BitmapUtils.rotateBitmapByExif(bitmap, exif);
            setBitmap = result.bitmap;
            degreesRotated = result.degrees;
            this.mInitialDegreesRotated = result.degrees;
        } else {
            setBitmap = bitmap;
        }

        this.mCropOverlayView.setInitialCropWindowRect((Rect) null);
        this.setBitmap(setBitmap, 0, (Uri) null, 1, degreesRotated);
    }

    public void setImageResource(int resId) {
        if (resId != 0) {
            this.mCropOverlayView.setInitialCropWindowRect((Rect) null);
            Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), resId);
            this.setBitmap(bitmap, resId, (Uri) null, 1, 0);
        }

    }

    public void setImageUriAsync(Uri uri) {
        if (uri != null) {
            BitmapLoadingWorkerTask currentTask = this.mBitmapLoadingWorkerTask != null ? (BitmapLoadingWorkerTask) this.mBitmapLoadingWorkerTask.get() : null;
            if (currentTask != null) {
                currentTask.cancel(true);
            }

            this.clearImageInt();
            this.mRestoreCropWindowRect = null;
            this.mRestoreDegreesRotated = 0;
            this.mCropOverlayView.setInitialCropWindowRect((Rect) null);
            this.mBitmapLoadingWorkerTask = new WeakReference(new BitmapLoadingWorkerTask(this, uri));
            ((BitmapLoadingWorkerTask) this.mBitmapLoadingWorkerTask.get()).execute(new Void[0]);
            this.setProgressBarVisibility();
        }

    }

    public void clearImage() {
        this.clearImageInt();
        this.mCropOverlayView.setInitialCropWindowRect((Rect) null);
    }

    public void rotateImage(int degrees) {
        if (this.mBitmap != null) {
            if (degrees < 0) {
                degrees = degrees % 360 + 360;
            } else {
                degrees %= 360;
            }

            boolean flipAxes = !this.mCropOverlayView.isFixAspectRatio() && (degrees > 45 && degrees < 135 || degrees > 215 && degrees < 305);
            BitmapUtils.RECT.set(this.mCropOverlayView.getCropWindowRect());
            float halfWidth = (flipAxes ? BitmapUtils.RECT.height() : BitmapUtils.RECT.width()) / 2.0F;
            float halfHeight = (flipAxes ? BitmapUtils.RECT.width() : BitmapUtils.RECT.height()) / 2.0F;
            if (flipAxes) {
                boolean isFlippedHorizontally = this.mFlipHorizontally;
                this.mFlipHorizontally = this.mFlipVertically;
                this.mFlipVertically = isFlippedHorizontally;
            }

            this.mImageMatrix.invert(this.mImageInverseMatrix);
            BitmapUtils.POINTS[0] = BitmapUtils.RECT.centerX();
            BitmapUtils.POINTS[1] = BitmapUtils.RECT.centerY();
            BitmapUtils.POINTS[2] = 0.0F;
            BitmapUtils.POINTS[3] = 0.0F;
            BitmapUtils.POINTS[4] = 1.0F;
            BitmapUtils.POINTS[5] = 0.0F;
            this.mImageInverseMatrix.mapPoints(BitmapUtils.POINTS);
            this.mDegreesRotated = (this.mDegreesRotated + degrees) % 360;
            this.applyImageMatrix((float) this.getWidth(), (float) this.getHeight(), true, false);
            this.mImageMatrix.mapPoints(BitmapUtils.POINTS2, BitmapUtils.POINTS);
            this.mZoom = (float) ((double) this.mZoom / Math.sqrt(Math.pow((double) (BitmapUtils.POINTS2[4] - BitmapUtils.POINTS2[2]), 2.0D) + Math.pow((double) (BitmapUtils.POINTS2[5] - BitmapUtils.POINTS2[3]), 2.0D)));
            this.mZoom = Math.max(this.mZoom, 1.0F);
            this.applyImageMatrix((float) this.getWidth(), (float) this.getHeight(), true, false);
            this.mImageMatrix.mapPoints(BitmapUtils.POINTS2, BitmapUtils.POINTS);
            double change = Math.sqrt(Math.pow((double) (BitmapUtils.POINTS2[4] - BitmapUtils.POINTS2[2]), 2.0D) + Math.pow((double) (BitmapUtils.POINTS2[5] - BitmapUtils.POINTS2[3]), 2.0D));
            halfWidth = (float) ((double) halfWidth * change);
            halfHeight = (float) ((double) halfHeight * change);
            BitmapUtils.RECT.set(BitmapUtils.POINTS2[0] - halfWidth, BitmapUtils.POINTS2[1] - halfHeight, BitmapUtils.POINTS2[0] + halfWidth, BitmapUtils.POINTS2[1] + halfHeight);
            this.mCropOverlayView.resetCropOverlayView();
            this.mCropOverlayView.setCropWindowRect(BitmapUtils.RECT);
            this.applyImageMatrix((float) this.getWidth(), (float) this.getHeight(), true, false);
            this.handleCropWindowChanged(false, false);
            this.mCropOverlayView.fixCurrentCropWindowRect();
        }

    }

    public void flipImageHorizontally() {
        this.mFlipHorizontally = !this.mFlipHorizontally;
        this.applyImageMatrix((float) this.getWidth(), (float) this.getHeight(), true, false);
    }

    public void flipImageVertically() {
        this.mFlipVertically = !this.mFlipVertically;
        this.applyImageMatrix((float) this.getWidth(), (float) this.getHeight(), true, false);
    }

    void onSetImageUriAsyncComplete(BitmapLoadingWorkerTask.Result result) {
        this.mBitmapLoadingWorkerTask = null;
        this.setProgressBarVisibility();
        if (result.error == null) {
            this.mInitialDegreesRotated = result.degreesRotated;
            this.setBitmap(result.bitmap, 0, result.uri, result.loadSampleSize, result.degreesRotated);
        }

        OnSetImageUriCompleteListener listener = this.mOnSetImageUriCompleteListener;
        if (listener != null) {
            listener.onSetImageUriComplete(this, result.uri, result.error);
        }

    }

    void onImageCroppingAsyncComplete(BitmapCroppingWorkerTask.Result result) {
        this.mBitmapCroppingWorkerTask = null;
        this.setProgressBarVisibility();
        OnCropImageCompleteListener listener = this.mOnCropImageCompleteListener;
        if (listener != null) {
            CropResult cropResult = new CropResult(this.mBitmap, this.mLoadedImageUri, result.bitmap, result.uri, result.error, this.getCropPoints(), this.getCropRect(), this.getRotatedDegrees(), result.sampleSize);
            listener.onCropImageComplete(this, cropResult);
        }

    }

    private void setBitmap(Bitmap bitmap, int imageResource, Uri imageUri, int loadSampleSize, int degreesRotated) {
        if (this.mBitmap == null || !this.mBitmap.equals(bitmap)) {
            this.mImageView.clearAnimation();
            this.clearImageInt();
            this.mBitmap = bitmap;
            this.mImageView.setImageBitmap(this.mBitmap);
            this.mLoadedImageUri = imageUri;
            this.mImageResource = imageResource;
            this.mLoadedSampleSize = loadSampleSize;
            this.mDegreesRotated = degreesRotated;
            this.applyImageMatrix((float) this.getWidth(), (float) this.getHeight(), true, false);
            if (this.mCropOverlayView != null) {
                this.mCropOverlayView.resetCropOverlayView();
                this.setCropOverlayVisibility();
            }
        }

    }

    private void clearImageInt() {
        if (this.mBitmap != null && (this.mImageResource > 0 || this.mLoadedImageUri != null)) {
            this.mBitmap.recycle();
        }

        this.mBitmap = null;
        this.mImageResource = 0;
        this.mLoadedImageUri = null;
        this.mLoadedSampleSize = 1;
        this.mDegreesRotated = 0;
        this.mZoom = 1.0F;
        this.mZoomOffsetX = 0.0F;
        this.mZoomOffsetY = 0.0F;
        this.mImageMatrix.reset();
        this.mSaveInstanceStateBitmapUri = null;
        this.mImageView.setImageBitmap((Bitmap) null);
        this.setCropOverlayVisibility();
    }

    public void startCropWorkerTask(int reqWidth, int reqHeight, RequestSizeOptions options, Uri saveUri, Bitmap.CompressFormat saveCompressFormat, int saveCompressQuality) {
        if (this.mBitmap != null) {
            this.mImageView.clearAnimation();
            BitmapCroppingWorkerTask currentTask = this.mBitmapCroppingWorkerTask != null ? (BitmapCroppingWorkerTask) this.mBitmapCroppingWorkerTask.get() : null;
            if (currentTask != null) {
                currentTask.cancel(true);
            }

            reqWidth = options != RequestSizeOptions.NONE ? reqWidth : 0;
            reqHeight = options != RequestSizeOptions.NONE ? reqHeight : 0;
            int orgWidth = this.mBitmap.getWidth() * this.mLoadedSampleSize;
            int orgHeight = this.mBitmap.getHeight() * this.mLoadedSampleSize;
            if (this.mLoadedImageUri == null || this.mLoadedSampleSize <= 1 && options != RequestSizeOptions.SAMPLING) {
                this.mBitmapCroppingWorkerTask = new WeakReference(new BitmapCroppingWorkerTask(this, this.mBitmap, this.getCropPoints(), this.mDegreesRotated, this.mCropOverlayView.isFixAspectRatio(), this.mCropOverlayView.getAspectRatioX(), this.mCropOverlayView.getAspectRatioY(), reqWidth, reqHeight, this.mFlipHorizontally, this.mFlipVertically, options, saveUri, saveCompressFormat, saveCompressQuality));
            } else {
                this.mBitmapCroppingWorkerTask = new WeakReference(new BitmapCroppingWorkerTask(this, this.mLoadedImageUri, this.getCropPoints(), this.mDegreesRotated, orgWidth, orgHeight, this.mCropOverlayView.isFixAspectRatio(), this.mCropOverlayView.getAspectRatioX(), this.mCropOverlayView.getAspectRatioY(), reqWidth, reqHeight, this.mFlipHorizontally, this.mFlipVertically, options, saveUri, saveCompressFormat, saveCompressQuality));
            }

            ((BitmapCroppingWorkerTask) this.mBitmapCroppingWorkerTask.get()).execute(new Void[0]);
            this.setProgressBarVisibility();
        }

    }

    public Parcelable onSaveInstanceState() {
        if (this.mLoadedImageUri == null && this.mBitmap == null && this.mImageResource < 1) {
            return super.onSaveInstanceState();
        } else {
            Bundle bundle = new Bundle();
            Uri imageUri = this.mLoadedImageUri;
            if (this.mSaveBitmapToInstanceState && imageUri == null && this.mImageResource < 1) {
                this.mSaveInstanceStateBitmapUri = imageUri = BitmapUtils.writeTempStateStoreBitmap(this.getContext(), this.mBitmap, this.mSaveInstanceStateBitmapUri);
            }

            if (imageUri != null && this.mBitmap != null) {
                String key = UUID.randomUUID().toString();
                BitmapUtils.mStateBitmap = new Pair(key, new WeakReference(this.mBitmap));
                bundle.putString("LOADED_IMAGE_STATE_BITMAP_KEY", key);
            }

            if (this.mBitmapLoadingWorkerTask != null) {
                BitmapLoadingWorkerTask task = (BitmapLoadingWorkerTask) this.mBitmapLoadingWorkerTask.get();
                if (task != null) {
                    bundle.putParcelable("LOADING_IMAGE_URI", task.getUri());
                }
            }

            bundle.putParcelable("instanceState", super.onSaveInstanceState());
            bundle.putParcelable("LOADED_IMAGE_URI", imageUri);
            bundle.putInt("LOADED_IMAGE_RESOURCE", this.mImageResource);
            bundle.putInt("LOADED_SAMPLE_SIZE", this.mLoadedSampleSize);
            bundle.putInt("DEGREES_ROTATED", this.mDegreesRotated);
            bundle.putParcelable("INITIAL_CROP_RECT", this.mCropOverlayView.getInitialCropWindowRect());
            BitmapUtils.RECT.set(this.mCropOverlayView.getCropWindowRect());
            this.mImageMatrix.invert(this.mImageInverseMatrix);
            this.mImageInverseMatrix.mapRect(BitmapUtils.RECT);
            bundle.putParcelable("CROP_WINDOW_RECT", BitmapUtils.RECT);
            bundle.putString("CROP_SHAPE", this.mCropOverlayView.getCropShape().name());
            bundle.putBoolean("CROP_AUTO_ZOOM_ENABLED", this.mAutoZoomEnabled);
            bundle.putInt("CROP_MAX_ZOOM", this.mMaxZoom);
            bundle.putBoolean("CROP_FLIP_HORIZONTALLY", this.mFlipHorizontally);
            bundle.putBoolean("CROP_FLIP_VERTICALLY", this.mFlipVertically);
            return bundle;
        }
    }

    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            if (this.mBitmapLoadingWorkerTask == null && this.mLoadedImageUri == null && this.mBitmap == null && this.mImageResource == 0) {
                Uri uri = (Uri) bundle.getParcelable("LOADED_IMAGE_URI");
                if (uri != null) {
                    String key = bundle.getString("LOADED_IMAGE_STATE_BITMAP_KEY");
                    if (key != null) {
                        Bitmap stateBitmap = BitmapUtils.mStateBitmap != null && ((String) BitmapUtils.mStateBitmap.first).equals(key) ? (Bitmap) ((WeakReference) BitmapUtils.mStateBitmap.second).get() : null;
                        BitmapUtils.mStateBitmap = null;
                        if (stateBitmap != null && !stateBitmap.isRecycled()) {
                            this.setBitmap(stateBitmap, 0, uri, bundle.getInt("LOADED_SAMPLE_SIZE"), 0);
                        }
                    }

                    if (this.mLoadedImageUri == null) {
                        this.setImageUriAsync(uri);
                    }
                } else {
                    int resId = bundle.getInt("LOADED_IMAGE_RESOURCE");
                    if (resId > 0) {
                        this.setImageResource(resId);
                    } else {
                        uri = (Uri) bundle.getParcelable("LOADING_IMAGE_URI");
                        if (uri != null) {
                            this.setImageUriAsync(uri);
                        }
                    }
                }

                this.mDegreesRotated = this.mRestoreDegreesRotated = bundle.getInt("DEGREES_ROTATED");
                Rect initialCropRect = (Rect) bundle.getParcelable("INITIAL_CROP_RECT");
                if (initialCropRect != null && (initialCropRect.width() > 0 || initialCropRect.height() > 0)) {
                    this.mCropOverlayView.setInitialCropWindowRect(initialCropRect);
                }

                RectF cropWindowRect = (RectF) bundle.getParcelable("CROP_WINDOW_RECT");
                if (cropWindowRect != null && (cropWindowRect.width() > 0.0F || cropWindowRect.height() > 0.0F)) {
                    this.mRestoreCropWindowRect = cropWindowRect;
                }

                this.mCropOverlayView.setCropShape(CropShape.valueOf(bundle.getString("CROP_SHAPE")));
                this.mAutoZoomEnabled = bundle.getBoolean("CROP_AUTO_ZOOM_ENABLED");
                this.mMaxZoom = bundle.getInt("CROP_MAX_ZOOM");
                this.mFlipHorizontally = bundle.getBoolean("CROP_FLIP_HORIZONTALLY");
                this.mFlipVertically = bundle.getBoolean("CROP_FLIP_VERTICALLY");
            }

            super.onRestoreInstanceState(bundle.getParcelable("instanceState"));
        } else {
            super.onRestoreInstanceState(state);
        }

    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (this.mBitmap != null) {
            if (heightSize == 0) {
                heightSize = this.mBitmap.getHeight();
            }

            double viewToBitmapWidthRatio = 1.0D / 0.0;
            double viewToBitmapHeightRatio = 1.0D / 0.0;
            if (widthSize < this.mBitmap.getWidth()) {
                viewToBitmapWidthRatio = (double) widthSize / (double) this.mBitmap.getWidth();
            }

            if (heightSize < this.mBitmap.getHeight()) {
                viewToBitmapHeightRatio = (double) heightSize / (double) this.mBitmap.getHeight();
            }

            int desiredWidth;
            int desiredHeight;
            if (viewToBitmapWidthRatio == 1.0D / 0.0 && viewToBitmapHeightRatio == 1.0D / 0.0) {
                desiredWidth = this.mBitmap.getWidth();
                desiredHeight = this.mBitmap.getHeight();
            } else if (viewToBitmapWidthRatio <= viewToBitmapHeightRatio) {
                desiredWidth = widthSize;
                desiredHeight = (int) ((double) this.mBitmap.getHeight() * viewToBitmapWidthRatio);
            } else {
                desiredHeight = heightSize;
                desiredWidth = (int) ((double) this.mBitmap.getWidth() * viewToBitmapHeightRatio);
            }

            int width = getOnMeasureSpec(widthMode, widthSize, desiredWidth);
            int height = getOnMeasureSpec(heightMode, heightSize, desiredHeight);
            this.mLayoutWidth = width;
            this.mLayoutHeight = height;
            this.setMeasuredDimension(this.mLayoutWidth, this.mLayoutHeight);
        } else {
            this.setMeasuredDimension(widthSize, heightSize);
        }

    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (this.mLayoutWidth > 0 && this.mLayoutHeight > 0) {
            LayoutParams origParams = (LayoutParams) this.getLayoutParams();
            origParams.width = this.mLayoutWidth;
            origParams.height = this.mLayoutHeight;
            this.setLayoutParams(origParams);
            if (this.mBitmap != null) {
                this.applyImageMatrix((float) (r - l), (float) (b - t), true, false);
                if (this.mRestoreCropWindowRect != null) {
                    if (this.mRestoreDegreesRotated != this.mInitialDegreesRotated) {
                        this.mDegreesRotated = this.mRestoreDegreesRotated;
                        this.applyImageMatrix((float) (r - l), (float) (b - t), true, false);
                    }

                    this.mImageMatrix.mapRect(this.mRestoreCropWindowRect);
                    this.mCropOverlayView.setCropWindowRect(this.mRestoreCropWindowRect);
                    this.handleCropWindowChanged(false, false);
                    this.mCropOverlayView.fixCurrentCropWindowRect();
                    this.mRestoreCropWindowRect = null;
                } else if (this.mSizeChanged) {
                    this.mSizeChanged = false;
                    this.handleCropWindowChanged(false, false);
                }
            } else {
                this.updateImageBounds(true);
            }
        } else {
            this.updateImageBounds(true);
        }

    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mSizeChanged = oldw > 0 && oldh > 0;
    }

    private void handleCropWindowChanged(boolean inProgress, boolean animate) {
        int width = this.getWidth();
        int height = this.getHeight();
        if (this.mBitmap != null && width > 0 && height > 0) {
            RectF cropRect = this.mCropOverlayView.getCropWindowRect();
            if (inProgress) {
                if (cropRect.left < 0.0F || cropRect.top < 0.0F || cropRect.right > (float) width || cropRect.bottom > (float) height) {
                    this.applyImageMatrix((float) width, (float) height, false, false);
                }
            } else if (this.mAutoZoomEnabled || this.mZoom > 1.0F) {
                float newZoom = 0.0F;
                if (this.mZoom < (float) this.mMaxZoom && cropRect.width() < (float) width * 0.5F && cropRect.height() < (float) height * 0.5F) {
                    newZoom = Math.min((float) this.mMaxZoom, Math.min((float) width / (cropRect.width() / this.mZoom / 0.64F), (float) height / (cropRect.height() / this.mZoom / 0.64F)));
                }

                if (this.mZoom > 1.0F && (cropRect.width() > (float) width * 0.65F || cropRect.height() > (float) height * 0.65F)) {
                    newZoom = Math.max(1.0F, Math.min((float) width / (cropRect.width() / this.mZoom / 0.51F), (float) height / (cropRect.height() / this.mZoom / 0.51F)));
                }

                if (!this.mAutoZoomEnabled) {
                    newZoom = 1.0F;
                }

                if (newZoom > 0.0F && newZoom != this.mZoom) {
                    if (animate) {
                        if (this.mAnimation == null) {
                            this.mAnimation = new CropImageAnimation(this.mImageView, this.mCropOverlayView);
                        }

                        this.mAnimation.setStartState(this.mImagePoints, this.mImageMatrix);
                    }

                    this.mZoom = newZoom;
                    this.applyImageMatrix((float) width, (float) height, true, animate);
                }
            }
        }

    }

    private void applyImageMatrix(float width, float height, boolean center, boolean animate) {
        if (this.mBitmap != null && width > 0.0F && height > 0.0F) {
            this.mImageMatrix.invert(this.mImageInverseMatrix);
            RectF cropRect = this.mCropOverlayView.getCropWindowRect();
            this.mImageInverseMatrix.mapRect(cropRect);
            this.mImageMatrix.reset();
            this.mImageMatrix.postTranslate((width - (float) this.mBitmap.getWidth()) / 2.0F, (height - (float) this.mBitmap.getHeight()) / 2.0F);
            this.mapImagePointsByImageMatrix();
            if (this.mDegreesRotated > 0) {
                this.mImageMatrix.postRotate((float) this.mDegreesRotated, BitmapUtils.getRectCenterX(this.mImagePoints), BitmapUtils.getRectCenterY(this.mImagePoints));
                this.mapImagePointsByImageMatrix();
            }

            float scale = Math.min(width / BitmapUtils.getRectWidth(this.mImagePoints), height / BitmapUtils.getRectHeight(this.mImagePoints));
            if (this.mScaleType == ScaleType.FIT_CENTER || this.mScaleType == ScaleType.CENTER_INSIDE && scale < 1.0F || scale > 1.0F && this.mAutoZoomEnabled) {
                this.mImageMatrix.postScale(scale, scale, BitmapUtils.getRectCenterX(this.mImagePoints), BitmapUtils.getRectCenterY(this.mImagePoints));
                this.mapImagePointsByImageMatrix();
            }

            float scaleX = this.mFlipHorizontally ? -this.mZoom : this.mZoom;
            float scaleY = this.mFlipVertically ? -this.mZoom : this.mZoom;
            this.mImageMatrix.postScale(scaleX, scaleY, BitmapUtils.getRectCenterX(this.mImagePoints), BitmapUtils.getRectCenterY(this.mImagePoints));
            this.mapImagePointsByImageMatrix();
            this.mImageMatrix.mapRect(cropRect);
            if (center) {
                this.mZoomOffsetX = width > BitmapUtils.getRectWidth(this.mImagePoints) ? 0.0F : Math.max(Math.min(width / 2.0F - cropRect.centerX(), -BitmapUtils.getRectLeft(this.mImagePoints)), (float) this.getWidth() - BitmapUtils.getRectRight(this.mImagePoints)) / scaleX;
                this.mZoomOffsetY = height > BitmapUtils.getRectHeight(this.mImagePoints) ? 0.0F : Math.max(Math.min(height / 2.0F - cropRect.centerY(), -BitmapUtils.getRectTop(this.mImagePoints)), (float) this.getHeight() - BitmapUtils.getRectBottom(this.mImagePoints)) / scaleY;
            } else {
                this.mZoomOffsetX = Math.min(Math.max(this.mZoomOffsetX * scaleX, -cropRect.left), -cropRect.right + width) / scaleX;
                this.mZoomOffsetY = Math.min(Math.max(this.mZoomOffsetY * scaleY, -cropRect.top), -cropRect.bottom + height) / scaleY;
            }

            this.mImageMatrix.postTranslate(this.mZoomOffsetX * scaleX, this.mZoomOffsetY * scaleY);
            cropRect.offset(this.mZoomOffsetX * scaleX, this.mZoomOffsetY * scaleY);
            this.mCropOverlayView.setCropWindowRect(cropRect);
            this.mapImagePointsByImageMatrix();
            this.mCropOverlayView.invalidate();
            if (animate) {
                this.mAnimation.setEndState(this.mImagePoints, this.mImageMatrix);
                this.mImageView.startAnimation(this.mAnimation);
            } else {
                this.mImageView.setImageMatrix(this.mImageMatrix);
            }

            this.updateImageBounds(false);
        }

    }

    private void mapImagePointsByImageMatrix() {
        this.mImagePoints[0] = 0.0F;
        this.mImagePoints[1] = 0.0F;
        this.mImagePoints[2] = (float) this.mBitmap.getWidth();
        this.mImagePoints[3] = 0.0F;
        this.mImagePoints[4] = (float) this.mBitmap.getWidth();
        this.mImagePoints[5] = (float) this.mBitmap.getHeight();
        this.mImagePoints[6] = 0.0F;
        this.mImagePoints[7] = (float) this.mBitmap.getHeight();
        this.mImageMatrix.mapPoints(this.mImagePoints);
    }

    private static int getOnMeasureSpec(int measureSpecMode, int measureSpecSize, int desiredSize) {
        int spec;
        if (measureSpecMode == 1073741824) {
            spec = measureSpecSize;
        } else if (measureSpecMode == -2147483648) {
            spec = Math.min(desiredSize, measureSpecSize);
        } else {
            spec = desiredSize;
        }

        return spec;
    }

    private void setCropOverlayVisibility() {
        if (this.mCropOverlayView != null) {
            this.mCropOverlayView.setVisibility(this.mShowCropOverlay && this.mBitmap != null ? VISIBLE : INVISIBLE);
        }

    }

    private void setProgressBarVisibility() {
        boolean visible = this.mShowProgressBar && (this.mBitmap == null && this.mBitmapLoadingWorkerTask != null || this.mBitmapCroppingWorkerTask != null);
        this.mProgressBar.setVisibility(visible ? VISIBLE : INVISIBLE);
    }

    private void updateImageBounds(boolean clear) {
        if (this.mBitmap != null && !clear) {
            float scaleFactorWidth = (float) (this.mBitmap.getWidth() * this.mLoadedSampleSize) / BitmapUtils.getRectWidth(this.mImagePoints);
            float scaleFactorHeight = (float) (this.mBitmap.getHeight() * this.mLoadedSampleSize) / BitmapUtils.getRectHeight(this.mImagePoints);
            this.mCropOverlayView.setCropWindowLimits((float) this.getWidth(), (float) this.getHeight(), scaleFactorWidth, scaleFactorHeight);
        }

        this.mCropOverlayView.setBounds(clear ? null : this.mImagePoints, this.getWidth(), this.getHeight());
    }

    public static class CropResult {
        private final Bitmap mOriginalBitmap;
        private final Uri mOriginalUri;
        private final Bitmap mBitmap;
        private final Uri mUri;
        private final Exception mError;
        private final float[] mCropPoints;
        private final Rect mCropRect;
        private final int mRotation;
        private final int mSampleSize;

        CropResult(Bitmap originalBitmap, Uri originalUri, Bitmap bitmap, Uri uri, Exception error, float[] cropPoints, Rect cropRect, int rotation, int sampleSize) {
            this.mOriginalBitmap = originalBitmap;
            this.mOriginalUri = originalUri;
            this.mBitmap = bitmap;
            this.mUri = uri;
            this.mError = error;
            this.mCropPoints = cropPoints;
            this.mCropRect = cropRect;
            this.mRotation = rotation;
            this.mSampleSize = sampleSize;
        }

        public Bitmap getOriginalBitmap() {
            return this.mOriginalBitmap;
        }

        public Uri getOriginalUri() {
            return this.mOriginalUri;
        }

        public boolean isSuccessful() {
            return this.mError == null;
        }

        public Bitmap getBitmap() {
            return this.mBitmap;
        }

        public Uri getUri() {
            return this.mUri;
        }

        public Exception getError() {
            return this.mError;
        }

        public float[] getCropPoints() {
            return this.mCropPoints;
        }

        public Rect getCropRect() {
            return this.mCropRect;
        }

        public int getRotation() {
            return this.mRotation;
        }

        public int getSampleSize() {
            return this.mSampleSize;
        }
    }

    public interface OnCropImageCompleteListener {
        void onCropImageComplete(CropImageView var1, CropResult var2);
    }

    public interface OnSetImageUriCompleteListener {
        void onSetImageUriComplete(CropImageView var1, Uri var2, Exception var3);
    }

    public interface OnSetCropOverlayReleasedListener {
        void onCropOverlayReleased(Rect var1);
    }

    public static enum RequestSizeOptions {
        NONE,
        SAMPLING,
        RESIZE_INSIDE,
        RESIZE_FIT,
        RESIZE_EXACT;

        private RequestSizeOptions() {
        }
    }

    public static enum Guidelines {
        OFF,
        ON_TOUCH,
        ON;

        private Guidelines() {
        }
    }

    public static enum ScaleType {
        FIT_CENTER,
        CENTER,
        CENTER_CROP,
        CENTER_INSIDE;

        private ScaleType() {
        }
    }

    public static enum CropShape {
        RECTANGLE,
        OVAL;

        private CropShape() {
        }
    }
}
