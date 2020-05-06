package iam.thevoid.mediapicker.chooser;

import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public abstract class ResolveInfoViewHolder extends RecyclerView.ViewHolder {

    ResolveInfoViewHolder(View itemView) {
        super(itemView);
    }

    protected abstract void onBind(PackageManager pm, ResolveInfo resolveInfo, int position);
}
