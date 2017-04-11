package iam.thevoid.mediapicker.chooser;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by iam on 03.04.17.
 */

public abstract class IntentAdapter extends RecyclerView.Adapter<ResolveInfoViewHolder> {

    PackageManager pm;
    List<ResolveInfo> resolveInfos;

    public IntentAdapter(PackageManager pm, Intent... intent) {
        this(pm, getResolveInfo(pm, intent));
    }

    public IntentAdapter(PackageManager pm, List<ResolveInfo> resolveInfos) {
        this.pm = pm;
        this.resolveInfos = resolveInfos;
    }

    @Override
    public void onBindViewHolder(ResolveInfoViewHolder holder, int position) {
        holder.onBind(pm, resolveInfos.get(position), position);
    }

    @Override
    public int getItemCount() {
        return resolveInfos == null ? 0 : resolveInfos.size();
    }

    protected static List<ResolveInfo> getResolveInfo(PackageManager pm, Intent[] intents) {
        if (intents == null || intents.length == 0) {
            return new ArrayList<>();
        }

        List<ResolveInfo> resolveInfos = new ArrayList<>();
        for (Intent i : intents) {
            resolveInfos.addAll(pm.queryIntentActivities(i, 0));
        }

        return resolveInfos;
    }

    protected static List<ResolveInfo> getResolveInfo(PackageManager pm, List<IntentData> intents) {
        if (intents == null || intents.size() == 0) {
            return new ArrayList<>();
        }

        List<ResolveInfo> resolveInfos = new ArrayList<>();
        for (IntentData i : intents) {
            resolveInfos.addAll(pm.queryIntentActivities(i.getIntent(), 0));
        }

        return resolveInfos;
    }
}
