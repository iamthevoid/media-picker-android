package iam.thevoid.mediapicker.chooser;

import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class IntentDataAdapter extends RecyclerView.Adapter<ResolveInfoViewHolder> {

    private HashMap<ResolveInfo, IntentData> resolveMap = new HashMap<>();
    private PackageManager pm;
    private List<ResolveInfo> resolveInfos;

    IntentDataAdapter(PackageManager packageManager, List<IntentData> intentDatas) {
        this.pm = packageManager;
        this.resolveInfos = getResolveInfo(packageManager, intentDatas, resolveMap);
    }

    @Override
    public void onBindViewHolder(@NonNull ResolveInfoViewHolder holder, int position) {
        holder.onBind(pm, resolveInfos.get(position), position);
    }

    @Override
    public int getItemCount() {
        return resolveInfos == null ? 0 : resolveInfos.size();
    }

    IntentData getIntentData(ResolveInfo info) {
        return resolveMap.get(info);
    }

    IntentData getIntentData(int position) {
        ResolveInfo info = resolveInfos.get(position);
        IntentData intentData = getIntentData(info);
        intentData.setApp(info);
        return intentData;
    }

    private static List<ResolveInfo> getResolveInfo(PackageManager pm, List<IntentData> intents,
                                                    HashMap<ResolveInfo, IntentData> resolveMap) {
        if (intents == null || intents.size() == 0) {
            return new ArrayList<>();
        }

        List<ResolveInfo> resolveInfos = new ArrayList<>();
        for (IntentData i : intents) {
            List<ResolveInfo> c = pm.queryIntentActivities(i.getIntent(), 0);
            for (ResolveInfo resolveInfo : c) {
                resolveMap.put(resolveInfo, i);
            }
            resolveInfos.addAll(c);
        }

        return resolveInfos;
    }
}
