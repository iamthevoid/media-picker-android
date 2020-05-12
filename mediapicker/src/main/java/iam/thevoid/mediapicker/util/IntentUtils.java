package iam.thevoid.mediapicker.util;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by iam on 22/08/2017.
 */

public final class IntentUtils {
    private IntentUtils() {}

    public static List<ResolveInfo> getResolveInfoList(PackageManager pm, List<Intent> intents) {
        if (intents == null || intents.size() == 0) {
            return new ArrayList<>();
        }

        List<ResolveInfo> resolveInfos = new ArrayList<>();

        for (Intent i : intents) {
            resolveInfos.addAll(pm.queryIntentActivities(i, 0));
        }

        return resolveInfos;
    }

    public static FragmentActivity getFragmentActivity(Context context) {
        if (context instanceof FragmentActivity) {
            return (FragmentActivity) context;
        } else if (context instanceof Activity) {
            throw new IllegalStateException("Context " + context + " NOT support-v4 Activity");
        } else if (context instanceof ContextWrapper) {
            return getFragmentActivity(((ContextWrapper) context).getBaseContext());
        }
        throw new IllegalStateException("Context " + context + " NOT contains activity!");
    }

    public static List<ResolveInfo> getResolveInfoList(PackageManager pm, Intent intent) {
        return getResolveInfoList(pm, Collections.singletonList(intent));
    }
}
