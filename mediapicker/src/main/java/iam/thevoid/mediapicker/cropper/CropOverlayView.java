package iam.thevoid.mediapicker.cropper;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.Arrays;

/**
 * Created by iam on 12/07/2017.
 */

public class CropOverlayView extends View {
    private ScaleGestureDetector mScaleDetector;
    private boolean mMultiTouchEnabled;
    private final CropWindowHandler mCropWindowHandler;
    private CropWindowChangeListener mCropWindowChangeListener;
    private final RectF mDrawRect;
    private Paint mBorderPaint;
    private Paint mBorderCornerPaint;
    private Paint mGuidelinePaint;
    private Paint mBackgroundPaint;
    private Path mPath;
    private final float[] mBoundsPoints;
    private final RectF mCalcBounds;
    private int mViewWidth;
    private int mViewHeight;
    private float mBorderCornerOffset;
    private float mBorderCornerLength;
    private float mInitialCropWindowPaddingRatio;
    private float mTouchRadius;
    private float mSnapRadius;
    private CropWindowMoveHandler mMoveHandler;
    private boolean mFixAspectRatio;
    private int mAspectRatioX;
    private int mAspectRatioY;
    private float mTargetAspectRatio;
    private CropImageView.Guidelines mGuidelines;
    private CropImageView.CropShape mCropShape;
    private final Rect mInitialCropWindowRect;
    private boolean initializedCropWindow;
    private Integer mOriginalLayerType;

    public CropOverlayView(Context context) {
        this(context, (AttributeSet) null);
    }

    public CropOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mCropWindowHandler = new CropWindowHandler();
        this.mDrawRect = new RectF();
        this.mPath = new Path();
        this.mBoundsPoints = new float[8];
        this.mCalcBounds = new RectF();
        this.mTargetAspectRatio = (float) this.mAspectRatioX / (float) this.mAspectRatioY;
        this.mInitialCropWindowRect = new Rect();
    }

    public void setCropWindowChangeListener(CropWindowChangeListener listener) {
        this.mCropWindowChangeListener = listener;
    }

    public RectF getCropWindowRect() {
        return this.mCropWindowHandler.getRect();
    }

    public void setCropWindowRect(RectF rect) {
        this.mCropWindowHandler.setRect(rect);
    }

    public void fixCurrentCropWindowRect() {
        RectF rect = this.getCropWindowRect();
        this.fixCropWindowRectByRules(rect);
        this.mCropWindowHandler.setRect(rect);
    }

    public void setBounds(float[] boundsPoints, int viewWidth, int viewHeight) {
        if (boundsPoints == null || !Arrays.equals(this.mBoundsPoints, boundsPoints)) {
            if (boundsPoints == null) {
                Arrays.fill(this.mBoundsPoints, 0.0F);
            } else {
                System.arraycopy(boundsPoints, 0, this.mBoundsPoints, 0, boundsPoints.length);
            }

            this.mViewWidth = viewWidth;
            this.mViewHeight = viewHeight;
            RectF cropRect = this.mCropWindowHandler.getRect();
            if (cropRect.width() == 0.0F || cropRect.height() == 0.0F) {
                this.initCropWindow();
            }
        }

    }

    public void resetCropOverlayView() {
        if (this.initializedCropWindow) {
            this.setCropWindowRect(BitmapUtils.EMPTY_RECT_F);
            this.initCropWindow();
            this.invalidate();
        }

    }

    public CropImageView.CropShape getCropShape() {
        return this.mCropShape;
    }

    public void setCropShape(CropImageView.CropShape cropShape) {
        if (this.mCropShape != cropShape) {
            this.mCropShape = cropShape;
            if (Build.VERSION.SDK_INT >= 11 && Build.VERSION.SDK_INT <= 17) {
                if (this.mCropShape == CropImageView.CropShape.OVAL) {
                    this.mOriginalLayerType = Integer.valueOf(this.getLayerType());
                    if (this.mOriginalLayerType.intValue() != 1) {
                        this.setLayerType(1, (Paint) null);
                    } else {
                        this.mOriginalLayerType = null;
                    }
                } else if (this.mOriginalLayerType != null) {
                    this.setLayerType(this.mOriginalLayerType.intValue(), (Paint) null);
                    this.mOriginalLayerType = null;
                }
            }

            this.invalidate();
        }

    }

    public CropImageView.Guidelines getGuidelines() {
        return this.mGuidelines;
    }

    public void setGuidelines(CropImageView.Guidelines guidelines) {
        if (this.mGuidelines != guidelines) {
            this.mGuidelines = guidelines;
            if (this.initializedCropWindow) {
                this.invalidate();
            }
        }

    }

    public boolean isFixAspectRatio() {
        return this.mFixAspectRatio;
    }

    public void setFixedAspectRatio(boolean fixAspectRatio) {
        if (this.mFixAspectRatio != fixAspectRatio) {
            this.mFixAspectRatio = fixAspectRatio;
            if (this.initializedCropWindow) {
                this.initCropWindow();
                this.invalidate();
            }
        }

    }

    public int getAspectRatioX() {
        return this.mAspectRatioX;
    }

    public void setAspectRatioX(int aspectRatioX) {
        if (aspectRatioX <= 0) {
            throw new IllegalArgumentException("Cannot set aspect ratio value to a number less than or equal to 0.");
        } else {
            if (this.mAspectRatioX != aspectRatioX) {
                this.mAspectRatioX = aspectRatioX;
                this.mTargetAspectRatio = (float) this.mAspectRatioX / (float) this.mAspectRatioY;
                if (this.initializedCropWindow) {
                    this.initCropWindow();
                    this.invalidate();
                }
            }

        }
    }

    public int getAspectRatioY() {
        return this.mAspectRatioY;
    }

    public void setAspectRatioY(int aspectRatioY) {
        if (aspectRatioY <= 0) {
            throw new IllegalArgumentException("Cannot set aspect ratio value to a number less than or equal to 0.");
        } else {
            if (this.mAspectRatioY != aspectRatioY) {
                this.mAspectRatioY = aspectRatioY;
                this.mTargetAspectRatio = (float) this.mAspectRatioX / (float) this.mAspectRatioY;
                if (this.initializedCropWindow) {
                    this.initCropWindow();
                    this.invalidate();
                }
            }

        }
    }

    public void setSnapRadius(float snapRadius) {
        this.mSnapRadius = snapRadius;
    }

    public boolean setMultiTouchEnabled(boolean multiTouchEnabled) {
        if (Build.VERSION.SDK_INT >= 11 && this.mMultiTouchEnabled != multiTouchEnabled) {
            this.mMultiTouchEnabled = multiTouchEnabled;
            if (this.mMultiTouchEnabled && this.mScaleDetector == null) {
                this.mScaleDetector = new ScaleGestureDetector(this.getContext(), new ScaleListener());
            }

            return true;
        } else {
            return false;
        }
    }

    public void setMinCropResultSize(int minCropResultWidth, int minCropResultHeight) {
        this.mCropWindowHandler.setMinCropResultSize(minCropResultWidth, minCropResultHeight);
    }

    public void setMaxCropResultSize(int maxCropResultWidth, int maxCropResultHeight) {
        this.mCropWindowHandler.setMaxCropResultSize(maxCropResultWidth, maxCropResultHeight);
    }

    public void setCropWindowLimits(float maxWidth, float maxHeight, float scaleFactorWidth, float scaleFactorHeight) {
        this.mCropWindowHandler.setCropWindowLimits(maxWidth, maxHeight, scaleFactorWidth, scaleFactorHeight);
    }

    public Rect getInitialCropWindowRect() {
        return this.mInitialCropWindowRect;
    }

    public void setInitialCropWindowRect(Rect rect) {
        this.mInitialCropWindowRect.set(rect != null ? rect : BitmapUtils.EMPTY_RECT);
        if (this.initializedCropWindow) {
            this.initCropWindow();
            this.invalidate();
            this.callOnCropWindowChanged(false);
        }

    }

    public void resetCropWindowRect() {
        if (this.initializedCropWindow) {
            this.initCropWindow();
            this.invalidate();
            this.callOnCropWindowChanged(false);
        }

    }

    public void setInitialAttributeValues(CropImageOptions options) {
        this.mCropWindowHandler.setInitialAttributeValues(options);
        this.setCropShape(options.cropShape);
        this.setSnapRadius(options.snapRadius);
        this.setGuidelines(options.guidelines);
        this.setFixedAspectRatio(options.fixAspectRatio);
        this.setAspectRatioX(options.aspectRatioX);
        this.setAspectRatioY(options.aspectRatioY);
        this.setMultiTouchEnabled(options.multiTouchEnabled);
        this.mTouchRadius = options.touchRadius;
        this.mInitialCropWindowPaddingRatio = options.initialCropWindowPaddingRatio;
        this.mBorderPaint = getNewPaintOrNull(options.borderLineThickness, options.borderLineColor);
        this.mBorderCornerOffset = options.borderCornerOffset;
        this.mBorderCornerLength = options.borderCornerLength;
        this.mBorderCornerPaint = getNewPaintOrNull(options.borderCornerThickness, options.borderCornerColor);
        this.mGuidelinePaint = getNewPaintOrNull(options.guidelinesThickness, options.guidelinesColor);
        this.mBackgroundPaint = getNewPaint(options.backgroundColor);
    }

    private void initCropWindow() {
        float leftLimit = Math.max(BitmapUtils.getRectLeft(this.mBoundsPoints), 0.0F);
        float topLimit = Math.max(BitmapUtils.getRectTop(this.mBoundsPoints), 0.0F);
        float rightLimit = Math.min(BitmapUtils.getRectRight(this.mBoundsPoints), (float) this.getWidth());
        float bottomLimit = Math.min(BitmapUtils.getRectBottom(this.mBoundsPoints), (float) this.getHeight());
        if (rightLimit > leftLimit && bottomLimit > topLimit) {
            RectF rect = new RectF();
            this.initializedCropWindow = true;
            float horizontalPadding = this.mInitialCropWindowPaddingRatio * (rightLimit - leftLimit);
            float verticalPadding = this.mInitialCropWindowPaddingRatio * (bottomLimit - topLimit);
            if (this.mInitialCropWindowRect.width() > 0 && this.mInitialCropWindowRect.height() > 0) {
                rect.left = leftLimit + (float) this.mInitialCropWindowRect.left / this.mCropWindowHandler.getScaleFactorWidth();
                rect.top = topLimit + (float) this.mInitialCropWindowRect.top / this.mCropWindowHandler.getScaleFactorHeight();
                rect.right = rect.left + (float) this.mInitialCropWindowRect.width() / this.mCropWindowHandler.getScaleFactorWidth();
                rect.bottom = rect.top + (float) this.mInitialCropWindowRect.height() / this.mCropWindowHandler.getScaleFactorHeight();
                rect.left = Math.max(leftLimit, rect.left);
                rect.top = Math.max(topLimit, rect.top);
                rect.right = Math.min(rightLimit, rect.right);
                rect.bottom = Math.min(bottomLimit, rect.bottom);
            } else if (this.mFixAspectRatio && rightLimit > leftLimit && bottomLimit > topLimit) {
                float bitmapAspectRatio = (rightLimit - leftLimit) / (bottomLimit - topLimit);
                float centerX;
                float cropWidth;
                float halfCropWidth;
                if (bitmapAspectRatio > this.mTargetAspectRatio) {
                    rect.top = topLimit + verticalPadding;
                    rect.bottom = bottomLimit - verticalPadding;
                    centerX = (float) this.getWidth() / 2.0F;
                    this.mTargetAspectRatio = (float) this.mAspectRatioX / (float) this.mAspectRatioY;
                    cropWidth = Math.max(this.mCropWindowHandler.getMinCropWidth(), rect.height() * this.mTargetAspectRatio);
                    halfCropWidth = cropWidth / 2.0F;
                    rect.left = centerX - halfCropWidth;
                    rect.right = centerX + halfCropWidth;
                } else {
                    rect.left = leftLimit + horizontalPadding;
                    rect.right = rightLimit - horizontalPadding;
                    centerX = (float) this.getHeight() / 2.0F;
                    cropWidth = Math.max(this.mCropWindowHandler.getMinCropHeight(), rect.width() / this.mTargetAspectRatio);
                    halfCropWidth = cropWidth / 2.0F;
                    rect.top = centerX - halfCropWidth;
                    rect.bottom = centerX + halfCropWidth;
                }
            } else {
                rect.left = leftLimit + horizontalPadding;
                rect.top = topLimit + verticalPadding;
                rect.right = rightLimit - horizontalPadding;
                rect.bottom = bottomLimit - verticalPadding;
            }

            this.fixCropWindowRectByRules(rect);
            this.mCropWindowHandler.setRect(rect);
        }
    }

    private void fixCropWindowRectByRules(RectF rect) {
        float adj;
        if (rect.width() < this.mCropWindowHandler.getMinCropWidth()) {
            adj = (this.mCropWindowHandler.getMinCropWidth() - rect.width()) / 2.0F;
            rect.left -= adj;
            rect.right += adj;
        }

        if (rect.height() < this.mCropWindowHandler.getMinCropHeight()) {
            adj = (this.mCropWindowHandler.getMinCropHeight() - rect.height()) / 2.0F;
            rect.top -= adj;
            rect.bottom += adj;
        }

        if (rect.width() > this.mCropWindowHandler.getMaxCropWidth()) {
            adj = (rect.width() - this.mCropWindowHandler.getMaxCropWidth()) / 2.0F;
            rect.left += adj;
            rect.right -= adj;
        }

        if (rect.height() > this.mCropWindowHandler.getMaxCropHeight()) {
            adj = (rect.height() - this.mCropWindowHandler.getMaxCropHeight()) / 2.0F;
            rect.top += adj;
            rect.bottom -= adj;
        }

        this.calculateBounds(rect);
        if (this.mCalcBounds.width() > 0.0F && this.mCalcBounds.height() > 0.0F) {
            adj = Math.max(this.mCalcBounds.left, 0.0F);
            float topLimit = Math.max(this.mCalcBounds.top, 0.0F);
            float rightLimit = Math.min(this.mCalcBounds.right, (float) this.getWidth());
            float bottomLimit = Math.min(this.mCalcBounds.bottom, (float) this.getHeight());
            if (rect.left < adj) {
                rect.left = adj;
            }

            if (rect.top < topLimit) {
                rect.top = topLimit;
            }

            if (rect.right > rightLimit) {
                rect.right = rightLimit;
            }

            if (rect.bottom > bottomLimit) {
                rect.bottom = bottomLimit;
            }
        }

        if (this.mFixAspectRatio && (double) Math.abs(rect.width() - rect.height() * this.mTargetAspectRatio) > 0.1D) {
            if (rect.width() > rect.height() * this.mTargetAspectRatio) {
                adj = Math.abs(rect.height() * this.mTargetAspectRatio - rect.width()) / 2.0F;
                rect.left += adj;
                rect.right -= adj;
            } else {
                adj = Math.abs(rect.width() / this.mTargetAspectRatio - rect.height()) / 2.0F;
                rect.top += adj;
                rect.bottom -= adj;
            }
        }

    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.drawBackground(canvas);
        if (this.mCropWindowHandler.showGuidelines()) {
            if (this.mGuidelines == CropImageView.Guidelines.ON) {
                this.drawGuidelines(canvas);
            } else if (this.mGuidelines == CropImageView.Guidelines.ON_TOUCH && this.mMoveHandler != null) {
                this.drawGuidelines(canvas);
            }
        }

        this.drawBorders(canvas);
        this.drawCorners(canvas);
    }

    private void drawBackground(Canvas canvas) {
        RectF rect = this.mCropWindowHandler.getRect();
        float left = Math.max(BitmapUtils.getRectLeft(this.mBoundsPoints), 0.0F);
        float top = Math.max(BitmapUtils.getRectTop(this.mBoundsPoints), 0.0F);
        float right = Math.min(BitmapUtils.getRectRight(this.mBoundsPoints), (float) this.getWidth());
        float bottom = Math.min(BitmapUtils.getRectBottom(this.mBoundsPoints), (float) this.getHeight());
        if (this.mCropShape == CropImageView.CropShape.RECTANGLE) {
            if (this.isNonStraightAngleRotated() && Build.VERSION.SDK_INT > 17) {
                this.mPath.reset();
                this.mPath.moveTo(this.mBoundsPoints[0], this.mBoundsPoints[1]);
                this.mPath.lineTo(this.mBoundsPoints[2], this.mBoundsPoints[3]);
                this.mPath.lineTo(this.mBoundsPoints[4], this.mBoundsPoints[5]);
                this.mPath.lineTo(this.mBoundsPoints[6], this.mBoundsPoints[7]);
                this.mPath.close();
                canvas.save();
                canvas.clipPath(this.mPath, Region.Op.INTERSECT);
                canvas.clipRect(rect, Region.Op.XOR);
                canvas.drawRect(left, top, right, bottom, this.mBackgroundPaint);
                canvas.restore();
            } else {
                canvas.drawRect(left, top, right, rect.top, this.mBackgroundPaint);
                canvas.drawRect(left, rect.bottom, right, bottom, this.mBackgroundPaint);
                canvas.drawRect(left, rect.top, rect.left, rect.bottom, this.mBackgroundPaint);
                canvas.drawRect(rect.right, rect.top, right, rect.bottom, this.mBackgroundPaint);
            }
        } else {
            this.mPath.reset();
            if (Build.VERSION.SDK_INT >= 11 && Build.VERSION.SDK_INT <= 17 && this.mCropShape == CropImageView.CropShape.OVAL) {
                this.mDrawRect.set(rect.left + 2.0F, rect.top + 2.0F, rect.right - 2.0F, rect.bottom - 2.0F);
            } else {
                this.mDrawRect.set(rect.left, rect.top, rect.right, rect.bottom);
            }

            this.mPath.addOval(this.mDrawRect, Path.Direction.CW);
            canvas.save();
            canvas.clipPath(this.mPath, Region.Op.XOR);
            canvas.drawRect(left, top, right, bottom, this.mBackgroundPaint);
            canvas.restore();
        }

    }

    private void drawGuidelines(Canvas canvas) {
        if (this.mGuidelinePaint != null) {
            float sw = this.mBorderPaint != null ? this.mBorderPaint.getStrokeWidth() : 0.0F;
            RectF rect = this.mCropWindowHandler.getRect();
            rect.inset(sw, sw);
            float oneThirdCropWidth = rect.width() / 3.0F;
            float oneThirdCropHeight = rect.height() / 3.0F;
            float w;
            float h;
            float x1;
            float x2;
            if (this.mCropShape == CropImageView.CropShape.OVAL) {
                w = rect.width() / 2.0F - sw;
                h = rect.height() / 2.0F - sw;
                x1 = rect.left + oneThirdCropWidth;
                x2 = rect.right - oneThirdCropWidth;
                float yv = (float) ((double) h * Math.sin(Math.acos((double) ((w - oneThirdCropWidth) / w))));
                canvas.drawLine(x1, rect.top + h - yv, x1, rect.bottom - h + yv, this.mGuidelinePaint);
                canvas.drawLine(x2, rect.top + h - yv, x2, rect.bottom - h + yv, this.mGuidelinePaint);
                float y1 = rect.top + oneThirdCropHeight;
                float y2 = rect.bottom - oneThirdCropHeight;
                float xv = (float) ((double) w * Math.cos(Math.asin((double) ((h - oneThirdCropHeight) / h))));
                canvas.drawLine(rect.left + w - xv, y1, rect.right - w + xv, y1, this.mGuidelinePaint);
                canvas.drawLine(rect.left + w - xv, y2, rect.right - w + xv, y2, this.mGuidelinePaint);
            } else {
                w = rect.left + oneThirdCropWidth;
                h = rect.right - oneThirdCropWidth;
                canvas.drawLine(w, rect.top, w, rect.bottom, this.mGuidelinePaint);
                canvas.drawLine(h, rect.top, h, rect.bottom, this.mGuidelinePaint);
                x1 = rect.top + oneThirdCropHeight;
                x2 = rect.bottom - oneThirdCropHeight;
                canvas.drawLine(rect.left, x1, rect.right, x1, this.mGuidelinePaint);
                canvas.drawLine(rect.left, x2, rect.right, x2, this.mGuidelinePaint);
            }
        }

    }

    private void drawBorders(Canvas canvas) {
        if (this.mBorderPaint != null) {
            float w = this.mBorderPaint.getStrokeWidth();
            RectF rect = this.mCropWindowHandler.getRect();
            rect.inset(w / 2.0F, w / 2.0F);
            if (this.mCropShape == CropImageView.CropShape.RECTANGLE) {
                canvas.drawRect(rect, this.mBorderPaint);
            } else {
                canvas.drawOval(rect, this.mBorderPaint);
            }
        }

    }

    private void drawCorners(Canvas canvas) {
        if (this.mBorderCornerPaint != null) {
            float lineWidth = this.mBorderPaint != null ? this.mBorderPaint.getStrokeWidth() : 0.0F;
            float cornerWidth = this.mBorderCornerPaint.getStrokeWidth();
            float w = cornerWidth / 2.0F + (this.mCropShape == CropImageView.CropShape.RECTANGLE ? this.mBorderCornerOffset : 0.0F);
            RectF rect = this.mCropWindowHandler.getRect();
            rect.inset(w, w);
            float cornerOffset = (cornerWidth - lineWidth) / 2.0F;
            float cornerExtension = cornerWidth / 2.0F + cornerOffset;
            canvas.drawLine(rect.left - cornerOffset, rect.top - cornerExtension, rect.left - cornerOffset, rect.top + this.mBorderCornerLength, this.mBorderCornerPaint);
            canvas.drawLine(rect.left - cornerExtension, rect.top - cornerOffset, rect.left + this.mBorderCornerLength, rect.top - cornerOffset, this.mBorderCornerPaint);
            canvas.drawLine(rect.right + cornerOffset, rect.top - cornerExtension, rect.right + cornerOffset, rect.top + this.mBorderCornerLength, this.mBorderCornerPaint);
            canvas.drawLine(rect.right + cornerExtension, rect.top - cornerOffset, rect.right - this.mBorderCornerLength, rect.top - cornerOffset, this.mBorderCornerPaint);
            canvas.drawLine(rect.left - cornerOffset, rect.bottom + cornerExtension, rect.left - cornerOffset, rect.bottom - this.mBorderCornerLength, this.mBorderCornerPaint);
            canvas.drawLine(rect.left - cornerExtension, rect.bottom + cornerOffset, rect.left + this.mBorderCornerLength, rect.bottom + cornerOffset, this.mBorderCornerPaint);
            canvas.drawLine(rect.right + cornerOffset, rect.bottom + cornerExtension, rect.right + cornerOffset, rect.bottom - this.mBorderCornerLength, this.mBorderCornerPaint);
            canvas.drawLine(rect.right + cornerExtension, rect.bottom + cornerOffset, rect.right - this.mBorderCornerLength, rect.bottom + cornerOffset, this.mBorderCornerPaint);
        }

    }

    private static Paint getNewPaint(int color) {
        Paint paint = new Paint();
        paint.setColor(color);
        return paint;
    }

    private static Paint getNewPaintOrNull(float thickness, int color) {
        if (thickness > 0.0F) {
            Paint borderPaint = new Paint();
            borderPaint.setColor(color);
            borderPaint.setStrokeWidth(thickness);
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setAntiAlias(true);
            return borderPaint;
        } else {
            return null;
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.isEnabled()) {
            if (this.mMultiTouchEnabled) {
                this.mScaleDetector.onTouchEvent(event);
            }

            switch (event.getAction()) {
                case 0:
                    this.onActionDown(event.getX(), event.getY());
                    return true;
                case 1:
                case 3:
                    this.getParent().requestDisallowInterceptTouchEvent(false);
                    this.onActionUp();
                    return true;
                case 2:
                    this.onActionMove(event.getX(), event.getY());
                    this.getParent().requestDisallowInterceptTouchEvent(true);
                    return true;
                default:
                    return false;
            }
        } else {
            return false;
        }
    }

    private void onActionDown(float x, float y) {
        this.mMoveHandler = this.mCropWindowHandler.getMoveHandler(x, y, this.mTouchRadius, this.mCropShape);
        if (this.mMoveHandler != null) {
            this.invalidate();
        }

    }

    private void onActionUp() {
        if (this.mMoveHandler != null) {
            this.mMoveHandler = null;
            this.callOnCropWindowChanged(false);
            this.invalidate();
        }

    }

    private void onActionMove(float x, float y) {
        if (this.mMoveHandler != null) {
            float snapRadius = this.mSnapRadius;
            RectF rect = this.mCropWindowHandler.getRect();
            if (this.calculateBounds(rect)) {
                snapRadius = 0.0F;
            }

            this.mMoveHandler.move(rect, x, y, this.mCalcBounds, this.mViewWidth, this.mViewHeight, snapRadius, this.mFixAspectRatio, this.mTargetAspectRatio);
            this.mCropWindowHandler.setRect(rect);
            this.callOnCropWindowChanged(true);
            this.invalidate();
        }

    }

    private boolean calculateBounds(RectF rect) {
        float left = BitmapUtils.getRectLeft(this.mBoundsPoints);
        float top = BitmapUtils.getRectTop(this.mBoundsPoints);
        float right = BitmapUtils.getRectRight(this.mBoundsPoints);
        float bottom = BitmapUtils.getRectBottom(this.mBoundsPoints);
        if (!this.isNonStraightAngleRotated()) {
            this.mCalcBounds.set(left, top, right, bottom);
            return false;
        } else {
            float x0 = this.mBoundsPoints[0];
            float y0 = this.mBoundsPoints[1];
            float x2 = this.mBoundsPoints[4];
            float y2 = this.mBoundsPoints[5];
            float x3 = this.mBoundsPoints[6];
            float y3 = this.mBoundsPoints[7];
            if (this.mBoundsPoints[7] < this.mBoundsPoints[1]) {
                if (this.mBoundsPoints[1] < this.mBoundsPoints[3]) {
                    x0 = this.mBoundsPoints[6];
                    y0 = this.mBoundsPoints[7];
                    x2 = this.mBoundsPoints[2];
                    y2 = this.mBoundsPoints[3];
                    x3 = this.mBoundsPoints[4];
                    y3 = this.mBoundsPoints[5];
                } else {
                    x0 = this.mBoundsPoints[4];
                    y0 = this.mBoundsPoints[5];
                    x2 = this.mBoundsPoints[0];
                    y2 = this.mBoundsPoints[1];
                    x3 = this.mBoundsPoints[2];
                    y3 = this.mBoundsPoints[3];
                }
            } else if (this.mBoundsPoints[1] > this.mBoundsPoints[3]) {
                x0 = this.mBoundsPoints[2];
                y0 = this.mBoundsPoints[3];
                x2 = this.mBoundsPoints[6];
                y2 = this.mBoundsPoints[7];
                x3 = this.mBoundsPoints[0];
                y3 = this.mBoundsPoints[1];
            }

            float a0 = (y3 - y0) / (x3 - x0);
            float a1 = -1.0F / a0;
            float b0 = y0 - a0 * x0;
            float b1 = y0 - a1 * x0;
            float b2 = y2 - a0 * x2;
            float b3 = y2 - a1 * x2;
            float c0 = (rect.centerY() - rect.top) / (rect.centerX() - rect.left);
            float c1 = -c0;
            float d0 = rect.top - c0 * rect.left;
            float d1 = rect.top - c1 * rect.right;
            left = Math.max(left, (d0 - b0) / (a0 - c0) < rect.right ? (d0 - b0) / (a0 - c0) : left);
            left = Math.max(left, (d0 - b1) / (a1 - c0) < rect.right ? (d0 - b1) / (a1 - c0) : left);
            left = Math.max(left, (d1 - b3) / (a1 - c1) < rect.right ? (d1 - b3) / (a1 - c1) : left);
            right = Math.min(right, (d1 - b1) / (a1 - c1) > rect.left ? (d1 - b1) / (a1 - c1) : right);
            right = Math.min(right, (d1 - b2) / (a0 - c1) > rect.left ? (d1 - b2) / (a0 - c1) : right);
            right = Math.min(right, (d0 - b2) / (a0 - c0) > rect.left ? (d0 - b2) / (a0 - c0) : right);
            top = Math.max(top, Math.max(a0 * left + b0, a1 * right + b1));
            bottom = Math.min(bottom, Math.min(a1 * left + b3, a0 * right + b2));
            this.mCalcBounds.left = left;
            this.mCalcBounds.top = top;
            this.mCalcBounds.right = right;
            this.mCalcBounds.bottom = bottom;
            return true;
        }
    }

    private boolean isNonStraightAngleRotated() {
        return this.mBoundsPoints[0] != this.mBoundsPoints[6] && this.mBoundsPoints[1] != this.mBoundsPoints[7];
    }

    private void callOnCropWindowChanged(boolean inProgress) {
        try {
            if (this.mCropWindowChangeListener != null) {
                this.mCropWindowChangeListener.onCropWindowChanged(inProgress);
            }
        } catch (Exception var3) {
            Log.e("AIC", "Exception in crop window changed", var3);
        }

    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        private ScaleListener() {
        }

        @TargetApi(11)
        public boolean onScale(ScaleGestureDetector detector) {
            RectF rect = CropOverlayView.this.mCropWindowHandler.getRect();
            float x = detector.getFocusX();
            float y = detector.getFocusY();
            float dY = detector.getCurrentSpanY() / 2.0F;
            float dX = detector.getCurrentSpanX() / 2.0F;
            float newTop = y - dY;
            float newLeft = x - dX;
            float newRight = x + dX;
            float newBottom = y + dY;
            if (newLeft < newRight && newTop <= newBottom && newLeft >= 0.0F && newRight <= CropOverlayView.this.mCropWindowHandler.getMaxCropWidth() && newTop >= 0.0F && newBottom <= CropOverlayView.this.mCropWindowHandler.getMaxCropHeight()) {
                rect.set(newLeft, newTop, newRight, newBottom);
                CropOverlayView.this.mCropWindowHandler.setRect(rect);
                CropOverlayView.this.invalidate();
            }

            return true;
        }
    }

    public interface CropWindowChangeListener {
        void onCropWindowChanged(boolean var1);
    }
}