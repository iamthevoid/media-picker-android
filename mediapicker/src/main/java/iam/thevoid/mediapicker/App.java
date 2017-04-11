package iam.thevoid.mediapicker;

import android.app.Application;

import com.orhanobut.hawk.Hawk;

/**
 * Created by iam on 11.04.17.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (!Hawk.isBuilt()) {
            Hawk.init(this).build();
        }
    }
}
