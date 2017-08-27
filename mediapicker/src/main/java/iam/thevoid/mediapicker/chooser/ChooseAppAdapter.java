package iam.thevoid.mediapicker.chooser;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import iam.thevoid.mediapicker.R;

/**
 * Created by iam on 03.04.17.
 */

public class ChooseAppAdapter extends IntentDataAdapter {

    private WeakReference<Context> contextWeakReference;

    private AdapterView.OnItemClickListener onItemClickListener;

    public ChooseAppAdapter(Context context, ArrayList<IntentData> intentDatas) {
        super(context.getPackageManager(), intentDatas);
        this.contextWeakReference = new WeakReference<>(context);
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public ResolveInfoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CustomAppChooserVH(LayoutInflater.from(contextWeakReference.get()).inflate(R.layout.app_select_item, parent, false));
    }

    protected class CustomAppChooserVH extends ResolveInfoViewHolder {

        private View view;

        ImageView imageView;
        TextView textView;

        public CustomAppChooserVH(View itemView) {
            super(itemView);
            this.view = itemView;
            this.imageView = (ImageView) view.findViewById(R.id.app_icon);
            this.textView = (TextView) view.findViewById(R.id.app_text);
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