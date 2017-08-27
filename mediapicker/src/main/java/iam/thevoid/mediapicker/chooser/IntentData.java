package iam.thevoid.mediapicker.chooser;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by iam on 03.04.17.
 */

public class IntentData implements Parcelable {

    private int requestCode;
    private int title;
    private Intent intent;

    public IntentData(Intent intent, int requestCode) {
        this(intent, requestCode, -1);
    }

    public IntentData(Intent intent, int requestCode, int title) {
        this.intent = intent;
        this.requestCode = requestCode;
        this.title = title;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public int getTitle() {
        return title;
    }

    public Intent getIntent() {
        return intent;
    }

    protected IntentData(Parcel in) {
        requestCode = in.readInt();
        title = in.readInt();
        intent = in.readParcelable(Intent.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(requestCode);
        dest.writeInt(title);
        dest.writeParcelable(intent, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<IntentData> CREATOR = new Creator<IntentData>() {
        @Override
        public IntentData createFromParcel(Parcel in) {
            return new IntentData(in);
        }

        @Override
        public IntentData[] newArray(int size) {
            return new IntentData[size];
        }
    };

    public void setApp(ResolveInfo app) {
        ActivityInfo activity = app.activityInfo;
        ComponentName name = new ComponentName(activity.applicationInfo.packageName,
                activity.name);
        this.intent.setComponent(name);
    }
}

