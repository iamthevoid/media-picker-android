package iam.thevoid.mediapicker.cropper;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;

/**
 * Created by iam on 12/07/2017.
 */

final class CropImageAnimation extends Animation implements Animation.AnimationListener {
    private final ImageView mImageView;
    private final CropOverlayView mCropOverlayView;
    private final float[] mStartBoundPoints = new float[8];
    private final float[] mEndBoundPoints = new float[8];
    private final RectF mStartCropWindowRect = new RectF();
    private final RectF mEndCropWindowRect = new RectF();
    private final float[] mStartImageMatrix = new float[9];
    private final float[] mEndImageMatrix = new float[9];
    private final RectF mAnimRect = new RectF();
    private final float[] mAnimPoints = new float[8];
    private final float[] mAnimMatrix = new float[9];

    CropImageAnimation(ImageView cropImageView, CropOverlayView cropOverlayView) {
        this.mImageView = cropImageView;
        this.mCropOverlayView = cropOverlayView;
        this.setDuration(300L);
        this.setFillAfter(true);
        this.setInterpolator(new AccelerateDecelerateInterpolator());
        this.setAnimationListener(this);
    }

    void setStartState(float[] boundPoints, Matrix imageMatrix) {
        this.reset();
        System.arraycopy(boundPoints, 0, this.mStartBoundPoints, 0, 8);
        this.mStartCropWindowRect.set(this.mCropOverlayView.getCropWindowRect());
        imageMatrix.getValues(this.mStartImageMatrix);
    }

    void setEndState(float[] boundPoints, Matrix imageMatrix) {
        System.arraycopy(boundPoints, 0, this.mEndBoundPoints, 0, 8);
        this.mEndCropWindowRect.set(this.mCropOverlayView.getCropWindowRect());
        imageMatrix.getValues(this.mEndImageMatrix);
    }

    protected void applyTransformation(float interpolatedTime, Transformation t) {
        this.mAnimRect.left = this.mStartCropWindowRect.left + (this.mEndCropWindowRect.left - this.mStartCropWindowRect.left) * interpolatedTime;
        this.mAnimRect.top = this.mStartCropWindowRect.top + (this.mEndCropWindowRect.top - this.mStartCropWindowRect.top) * interpolatedTime;
        this.mAnimRect.right = this.mStartCropWindowRect.right + (this.mEndCropWindowRect.right - this.mStartCropWindowRect.right) * interpolatedTime;
        this.mAnimRect.bottom = this.mStartCropWindowRect.bottom + (this.mEndCropWindowRect.bottom - this.mStartCropWindowRect.bottom) * interpolatedTime;
        this.mCropOverlayView.setCropWindowRect(this.mAnimRect);

        int i;
        for (i = 0; i < this.mAnimPoints.length; ++i) {
            this.mAnimPoints[i] = this.mStartBoundPoints[i] + (this.mEndBoundPoints[i] - this.mStartBoundPoints[i]) * interpolatedTime;
        }

        this.mCropOverlayView.setBounds(this.mAnimPoints, this.mImageView.getWidth(), this.mImageView.getHeight());

        for (i = 0; i < this.mAnimMatrix.length; ++i) {
            this.mAnimMatrix[i] = this.mStartImageMatrix[i] + (this.mEndImageMatrix[i] - this.mStartImageMatrix[i]) * interpolatedTime;
        }

        Matrix m = this.mImageView.getImageMatrix();
        m.setValues(this.mAnimMatrix);
        this.mImageView.setImageMatrix(m);
        this.mImageView.invalidate();
        this.mCropOverlayView.invalidate();
    }

    public void onAnimationStart(Animation animation) {
    }

    public void onAnimationEnd(Animation animation) {
        this.mImageView.clearAnimation();
    }

    public void onAnimationRepeat(Animation animation) {
    }
}