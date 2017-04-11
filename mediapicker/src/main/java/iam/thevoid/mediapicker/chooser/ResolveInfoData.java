package iam.thevoid.mediapicker.chooser;

import android.content.pm.ResolveInfo;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by iam on 03.04.17.
 */

public class ResolveInfoData implements Parcelable {

    private int requestCode;
    private ResolveInfo resolveInfo;

    public ResolveInfoData(ResolveInfo info) {
        this(info, -1);
    }

    public ResolveInfoData(ResolveInfo info, int requestCode) {
        this.resolveInfo = info;
        this.requestCode = requestCode;
    }

    protected ResolveInfoData(Parcel in) {
        requestCode = in.readInt();
        resolveInfo = in.readParcelable(ResolveInfo.class.getClassLoader());
    }

    public int getRequestCode() {
        return requestCode;
    }

    public ResolveInfo getResolveInfo() {
        return resolveInfo;
    }

    public static final Creator<ResolveInfoData> CREATOR = new Creator<ResolveInfoData>() {
        @Override
        public ResolveInfoData createFromParcel(Parcel in) {
            return new ResolveInfoData(in);
        }

        @Override
        public ResolveInfoData[] newArray(int size) {
            return new ResolveInfoData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(requestCode);
        dest.writeParcelable(resolveInfo, flags);
    }
}
