package iam.thevoid.mediapicker.cropper;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import iam.thevoid.mediapicker.R;

/**
 * Created by iam on 12/07/2017.
 */

public class CropImageOptions implements Parcelable {
    public static final Creator<CropImageOptions> CREATOR = new Creator<CropImageOptions>() {
        public CropImageOptions createFromParcel(Parcel in) {
            return new CropImageOptions(in);
        }

        public CropImageOptions[] newArray(int size) {
            return new CropImageOptions[size];
        }
    };
    public CropImageView.CropShape cropShape;
    public float snapRadius;
    public float touchRadius;
    public CropImageView.Guidelines guidelines;
    public CropImageView.ScaleType scaleType;
    public boolean showCropOverlay;
    public boolean showProgressBar;
    public boolean autoZoomEnabled;
    public boolean multiTouchEnabled;
    public int maxZoom;
    public float initialCropWindowPaddingRatio;
    public boolean fixAspectRatio;
    public int aspectRatioX;
    public int aspectRatioY;
    public float borderLineThickness;
    public int borderLineColor;
    public float borderCornerThickness;
    public float borderCornerOffset;
    public float borderCornerLength;
    public int borderCornerColor;
    public float guidelinesThickness;
    public int guidelinesColor;
    public int backgroundColor;
    public int minCropWindowWidth;
    public int minCropWindowHeight;
    public int minCropResultWidth;
    public int minCropResultHeight;
    public int maxCropResultWidth;
    public int maxCropResultHeight;
    public String activityTitle;
    public int activityMenuIconColor;
    public Uri outputUri;
    public Bitmap.CompressFormat outputCompressFormat;
    public int outputCompressQuality;
    public int outputRequestWidth;
    public int outputRequestHeight;
    public CropImageView.RequestSizeOptions outputRequestSizeOptions;
    public boolean noOutputImage;
    public Rect initialCropWindowRectangle;
    public int initialRotation;
    public boolean allowRotation;
    public boolean allowFlipping;
    public boolean allowCounterRotation;
    public int rotationDegrees;
    public boolean flipHorizontally;
    public boolean flipVertically;

    public CropImageOptions() {
        DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();
        this.cropShape = CropImageView.CropShape.RECTANGLE;
        this.snapRadius = TypedValue.applyDimension(1, 3.0F, dm);
        this.touchRadius = TypedValue.applyDimension(1, 24.0F, dm);
        this.guidelines = CropImageView.Guidelines.ON_TOUCH;
        this.scaleType = CropImageView.ScaleType.FIT_CENTER;
        this.showCropOverlay = true;
        this.showProgressBar = true;
        this.autoZoomEnabled = true;
        this.multiTouchEnabled = false;
        this.maxZoom = 4;
        this.initialCropWindowPaddingRatio = 0.1F;
        this.fixAspectRatio = false;
        this.aspectRatioX = 1;
        this.aspectRatioY = 1;
        this.borderLineThickness = TypedValue.applyDimension(1, 3.0F, dm);
        this.borderLineColor = Color.argb(170, 255, 255, 255);
        this.borderCornerThickness = TypedValue.applyDimension(1, 2.0F, dm);
        this.borderCornerOffset = TypedValue.applyDimension(1, 5.0F, dm);
        this.borderCornerLength = TypedValue.applyDimension(1, 14.0F, dm);
        this.borderCornerColor = -1;
        this.guidelinesThickness = TypedValue.applyDimension(1, 1.0F, dm);
        this.guidelinesColor = Color.argb(170, 255, 255, 255);
        this.backgroundColor = Color.argb(119, 0, 0, 0);
        this.minCropWindowWidth = (int) TypedValue.applyDimension(1, 42.0F, dm);
        this.minCropWindowHeight = (int) TypedValue.applyDimension(1, 42.0F, dm);
        this.minCropResultWidth = 40;
        this.minCropResultHeight = 40;
        this.maxCropResultWidth = 99999;
        this.maxCropResultHeight = 99999;
        this.activityTitle = "";
        this.activityMenuIconColor = Color.WHITE;
        this.outputUri = Uri.EMPTY;
        this.outputCompressFormat = Bitmap.CompressFormat.JPEG;
        this.outputCompressQuality = 90;
        this.outputRequestWidth = 0;
        this.outputRequestHeight = 0;
        this.outputRequestSizeOptions = CropImageView.RequestSizeOptions.NONE;
        this.noOutputImage = false;
        this.initialCropWindowRectangle = null;
        this.initialRotation = -1;
        this.allowRotation = true;
        this.allowFlipping = true;
        this.allowCounterRotation = false;
        this.rotationDegrees = 90;
        this.flipHorizontally = false;
        this.flipVertically = false;
    }

    protected CropImageOptions(Parcel in) {
        this.cropShape = CropImageView.CropShape.values()[in.readInt()];
        this.snapRadius = in.readFloat();
        this.touchRadius = in.readFloat();
        this.guidelines = CropImageView.Guidelines.values()[in.readInt()];
        this.scaleType = CropImageView.ScaleType.values()[in.readInt()];
        this.showCropOverlay = in.readByte() != 0;
        this.showProgressBar = in.readByte() != 0;
        this.autoZoomEnabled = in.readByte() != 0;
        this.multiTouchEnabled = in.readByte() != 0;
        this.maxZoom = in.readInt();
        this.initialCropWindowPaddingRatio = in.readFloat();
        this.fixAspectRatio = in.readByte() != 0;
        this.aspectRatioX = in.readInt();
        this.aspectRatioY = in.readInt();
        this.borderLineThickness = in.readFloat();
        this.borderLineColor = in.readInt();
        this.borderCornerThickness = in.readFloat();
        this.borderCornerOffset = in.readFloat();
        this.borderCornerLength = in.readFloat();
        this.borderCornerColor = in.readInt();
        this.guidelinesThickness = in.readFloat();
        this.guidelinesColor = in.readInt();
        this.backgroundColor = in.readInt();
        this.minCropWindowWidth = in.readInt();
        this.minCropWindowHeight = in.readInt();
        this.minCropResultWidth = in.readInt();
        this.minCropResultHeight = in.readInt();
        this.maxCropResultWidth = in.readInt();
        this.maxCropResultHeight = in.readInt();
        this.activityTitle = in.readString();
        this.activityMenuIconColor = in.readInt();
        this.outputUri = in.readParcelable(Uri.class.getClassLoader());
        this.outputCompressFormat = Bitmap.CompressFormat.valueOf(in.readString());
        this.outputCompressQuality = in.readInt();
        this.outputRequestWidth = in.readInt();
        this.outputRequestHeight = in.readInt();
        this.outputRequestSizeOptions = CropImageView.RequestSizeOptions.values()[in.readInt()];
        this.noOutputImage = in.readByte() != 0;
        this.initialCropWindowRectangle = in.readParcelable(Rect.class.getClassLoader());
        this.initialRotation = in.readInt();
        this.allowRotation = in.readByte() != 0;
        this.allowFlipping = in.readByte() != 0;
        this.allowCounterRotation = in.readByte() != 0;
        this.rotationDegrees = in.readInt();
        this.flipHorizontally = in.readByte() != 0;
        this.flipVertically = in.readByte() != 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.cropShape.ordinal());
        dest.writeFloat(this.snapRadius);
        dest.writeFloat(this.touchRadius);
        dest.writeInt(this.guidelines.ordinal());
        dest.writeInt(this.scaleType.ordinal());
        dest.writeByte((byte) (this.showCropOverlay ? 1 : 0));
        dest.writeByte((byte) (this.showProgressBar ? 1 : 0));
        dest.writeByte((byte) (this.autoZoomEnabled ? 1 : 0));
        dest.writeByte((byte) (this.multiTouchEnabled ? 1 : 0));
        dest.writeInt(this.maxZoom);
        dest.writeFloat(this.initialCropWindowPaddingRatio);
        dest.writeByte((byte) (this.fixAspectRatio ? 1 : 0));
        dest.writeInt(this.aspectRatioX);
        dest.writeInt(this.aspectRatioY);
        dest.writeFloat(this.borderLineThickness);
        dest.writeInt(this.borderLineColor);
        dest.writeFloat(this.borderCornerThickness);
        dest.writeFloat(this.borderCornerOffset);
        dest.writeFloat(this.borderCornerLength);
        dest.writeInt(this.borderCornerColor);
        dest.writeFloat(this.guidelinesThickness);
        dest.writeInt(this.guidelinesColor);
        dest.writeInt(this.backgroundColor);
        dest.writeInt(this.minCropWindowWidth);
        dest.writeInt(this.minCropWindowHeight);
        dest.writeInt(this.minCropResultWidth);
        dest.writeInt(this.minCropResultHeight);
        dest.writeInt(this.maxCropResultWidth);
        dest.writeInt(this.maxCropResultHeight);
        dest.writeString(this.activityTitle);
        dest.writeInt(this.activityMenuIconColor);
        dest.writeParcelable(this.outputUri, flags);
        dest.writeString(this.outputCompressFormat.name());
        dest.writeInt(this.outputCompressQuality);
        dest.writeInt(this.outputRequestWidth);
        dest.writeInt(this.outputRequestHeight);
        dest.writeInt(this.outputRequestSizeOptions.ordinal());
        dest.writeInt(this.noOutputImage ? 1 : 0);
        dest.writeParcelable(this.initialCropWindowRectangle, flags);
        dest.writeInt(this.initialRotation);
        dest.writeByte((byte) (this.allowRotation ? 1 : 0));
        dest.writeByte((byte) (this.allowFlipping ? 1 : 0));
        dest.writeByte((byte) (this.allowCounterRotation ? 1 : 0));
        dest.writeInt(this.rotationDegrees);
        dest.writeByte((byte) (this.flipHorizontally ? 1 : 0));
        dest.writeByte((byte) (this.flipVertically ? 1 : 0));
    }

    public int describeContents() {
        return 0;
    }

    void validate() {
        if (this.maxZoom < 0) {
            throw new IllegalArgumentException("Cannot set max zoom to a number < 1");
        } else if (this.touchRadius < 0.0F) {
            throw new IllegalArgumentException("Cannot set touch radius value to a number <= 0 ");
        } else if (this.initialCropWindowPaddingRatio >= 0.0F && (double) this.initialCropWindowPaddingRatio < 0.5D) {
            if (this.aspectRatioX <= 0) {
                throw new IllegalArgumentException("Cannot set aspect ratio value to a number less than or equal to 0.");
            } else if (this.aspectRatioY <= 0) {
                throw new IllegalArgumentException("Cannot set aspect ratio value to a number less than or equal to 0.");
            } else if (this.borderLineThickness < 0.0F) {
                throw new IllegalArgumentException("Cannot set line thickness value to a number less than 0.");
            } else if (this.borderCornerThickness < 0.0F) {
                throw new IllegalArgumentException("Cannot set corner thickness value to a number less than 0.");
            } else if (this.guidelinesThickness < 0.0F) {
                throw new IllegalArgumentException("Cannot set guidelines thickness value to a number less than 0.");
            } else if (this.minCropWindowHeight < 0) {
                throw new IllegalArgumentException("Cannot set min crop window height value to a number < 0 ");
            } else if (this.minCropResultWidth < 0) {
                throw new IllegalArgumentException("Cannot set min crop result width value to a number < 0 ");
            } else if (this.minCropResultHeight < 0) {
                throw new IllegalArgumentException("Cannot set min crop result height value to a number < 0 ");
            } else if (this.maxCropResultWidth < this.minCropResultWidth) {
                throw new IllegalArgumentException("Cannot set max crop result width to smaller value than min crop result width");
            } else if (this.maxCropResultHeight < this.minCropResultHeight) {
                throw new IllegalArgumentException("Cannot set max crop result height to smaller value than min crop result height");
            } else if (this.outputRequestWidth < 0) {
                throw new IllegalArgumentException("Cannot set request width value to a number < 0 ");
            } else if (this.outputRequestHeight < 0) {
                throw new IllegalArgumentException("Cannot set request height value to a number < 0 ");
            } else if (this.rotationDegrees < 0 || this.rotationDegrees > 360) {
                throw new IllegalArgumentException("Cannot set rotation degrees value to a number < 0 or > 360");
            }
        } else {
            throw new IllegalArgumentException("Cannot set initial crop window padding value to a number < 0 or >= 0.5");
        }
    }
}