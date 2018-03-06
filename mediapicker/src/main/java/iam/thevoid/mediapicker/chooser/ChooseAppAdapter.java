package iam.thevoid.mediapicker.chooser;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.List;

import iam.thevoid.mediapicker.R;

public class ChooseAppAdapter extends IntentDataAdapter {

    private WeakReference<Context> contextWeakReference;

    private AdapterView.OnItemClickListener onItemClickListener;

    ChooseAppAdapter(Context context, List<IntentData> intentDatas) {
        super(context.getPackageManager(), intentDatas);
        this.contextWeakReference = new WeakReference<>(context);
    }

    void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ResolveInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CustomAppChooserVH(LayoutInflater.from(contextWeakReference.get()).inflate(R.layout.app_select_item, parent, false));
    }

    protected class CustomAppChooserVH extends ResolveInfoViewHolder {

        private View view;

        ImageView imageView;
        TextView textView;

        CustomAppChooserVH(View itemView) {
            super(itemView);
            this.view = itemView;
            this.imageView = view.findViewById(R.id.app_icon);
            this.textView = view.findViewById(R.id.app_text);
        }

        @Override
        public void onBind(PackageManager pm, ResolveInfo resolveInfo, int position) {
            imageView.setImageDrawable(resolveInfo.loadIcon(pm));

            IntentData intentData = getIntentData(resolveInfo);

            int title = intentData != null ? intentData.getTitle() : -1;

            textView.setText(title == -1 ? resolveInfo.loadLabel(pm) : contextWeakReference.get().getString(title));

            view.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(null, view, position, position + 1);
                }
            });
        }
    }
}