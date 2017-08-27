package iam.thevoid.mediapicker.chooser;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.Collections;

import iam.thevoid.mediapicker.R;
import iam.thevoid.mediapicker.util.IntentUtils;


/**
 * Created by iam on 03.04.17.
 */

public class MediaPickSelectAppDialog extends BottomSheetDialogFragment implements AdapterView.OnItemClickListener {

    public static final String EXTRA_RESOLVE = "EXTRA_RESOLVE";

    RecyclerView recyclerView;

    private ChooseAppAdapter adapter;

    private OnSelectaAppCallback callback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.about_dialog, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setVerticalScrollBarEnabled(true);

        ArrayList<IntentData> parcelableArrayList = getArguments().getParcelableArrayList(EXTRA_RESOLVE);


        adapter = new ChooseAppAdapter(getActivity(), parcelableArrayList);
        adapter.setOnItemClickListener(this);

        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        dismiss();
        if (callback != null) {
            callback.onAppSelect(adapter.getIntentData(position));
        }
    }


    public static void showForResult(Context context, ArrayList<IntentData> intents, OnSelectaAppCallback callback) {
        show(IntentUtils.getFragmentActivity(context).getSupportFragmentManager(),
                intents,
                callback);
    }

    private static <T extends IntentData> void show(
            FragmentManager fm,
            ArrayList<T> data,
            OnSelectaAppCallback callback) {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(EXTRA_RESOLVE, data);
        MediaPickSelectAppDialog fragment = new MediaPickSelectAppDialog();
        fragment.callback = callback;
        fragment.setArguments(bundle);
        fragment.show(fm);
    }

    public void show(FragmentManager fragmentManager) {
        show(fragmentManager, this.getClass().getCanonicalName());
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        try {
            super.show(manager, tag);
        } catch (IllegalStateException e) {
            // ignore
        }
    }

    private static ArrayList<IntentData> makeList(IntentData... intentDatas) {
        ArrayList<IntentData> ids = new ArrayList<>();
        Collections.addAll(ids, intentDatas);
        return ids;
    }

    public interface OnSelectaAppCallback {
        void onAppSelect(IntentData intentData);
    }
}
