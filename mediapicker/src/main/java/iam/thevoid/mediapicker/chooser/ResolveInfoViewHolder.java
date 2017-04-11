package iam.thevoid.mediapicker.chooser;

import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by iam on 03.04.17.
 */

public abstract class ResolveInfoViewHolder extends RecyclerView.ViewHolder {

    public ResolveInfoViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void onBind(PackageManager pm, ResolveInfo resolveInfo, int position);
}
