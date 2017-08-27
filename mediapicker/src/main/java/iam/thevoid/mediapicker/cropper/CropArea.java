package iam.thevoid.mediapicker.cropper;

import android.os.Parcel;
import android.os.Parcelable;

import static iam.thevoid.mediapicker.cropper.CropImageView.CropShape.OVAL;
import static iam.thevoid.mediapicker.cropper.CropImageView.CropShape.RECTANGLE;

/**
 * Created by iam on 21.04.17.
 */

public class CropArea implements Parcelable {

    private CropImageView.CropShape cropShape = RECTANGLE;
    private final int widthRatio;
    private final int heightRatio;
    private final boolean determinate;

    private CropArea() {
        this(RECTANGLE);
    }

    private CropArea(CropImageView.CropShape cropShape) {
        this(1, 1, cropShape, true);
    }

    private CropArea(int width, int height) {
        this(width, height, RECTANGLE, true);
    }

    private CropArea(boolean determinate) {
        this(1, 1, RECTANGLE, determinate);
    }

    private CropArea(int width, int height, CropImageView.CropShape cropShape, boolean determinate) {
        int tempWidth = width;
        int tempHeight = height;
        this.cropShape = cropShape;
        this.determinate = determinate;

        if (width <= 1) {
            tempWidth = 1;
        }

        if (tempHeight <= 1) {
            tempHeight = 1;
        }

        int greaterCommonDivisor = greatestCommonDivisor(tempHeight, tempWidth);

        this.widthRatio = tempWidth / greaterCommonDivisor;
        this.heightRatio = tempHeight / greaterCommonDivisor;
    }

    protected CropArea(Parcel in) {
        cropShape = CropImageView.CropShape.values()[in.readInt()];
        widthRatio = in.readInt();
        heightRatio = in.readInt();
        determinate = in.readByte() != 0;
    }

    public static final Creator<CropArea> CREATOR = new Creator<CropArea>() {
        @Override
        public CropArea createFromParcel(Parcel in) {
            return new CropArea(in);
        }

        @Override
        public CropArea[] newArray(int size) {
            return new CropArea[size];
        }
    };

    public boolean isDeterminate() {
        return determinate;
    }

    private int greatestCommonDivisor(int a, int b) {
        int max = a < b ? b : a;
        int min = a < b ? a : b;

        if (max % min == 0) {
            return min;
        }

        int gcd = 1;

        for (int i = 2; i < min; i++) {
            if (max % i == 0 && min % i == 0) {
                gcd = i;
            }
        }

        return gcd;
    }

    public CropImageView.CropShape getCropShape() {
        return cropShape;
    }

    public int getWidthRatio() {
        return widthRatio;
    }

    public int getHeightRatio() {
        return heightRatio;
    }

    public static CropArea square() {
        return new CropArea();
    }

    public static CropArea circle() {
        return new CropArea(OVAL);
    }

    public static CropArea ratio(int widthRatio, int heightRatio) {
        return new CropArea(widthRatio, heightRatio);
    }

    public static CropArea indeterminate() {
        return new CropArea(false);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(cropShape.ordinal());
        dest.writeInt(widthRatio);
        dest.writeInt(heightRatio);
        dest.writeByte((byte) (determinate ? 1 : 0));
    }
}
