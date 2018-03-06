package iam.thevoid.mediapicker.chooser;

import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public abstract class ResolveInfoViewHolder extends RecyclerView.ViewHolder {

    ResolveInfoViewHolder(View itemView) {
        super(itemView);
    }

    protected abstract void onBind(PackageManager pm, ResolveInfo resolveInfo, int position);
}
