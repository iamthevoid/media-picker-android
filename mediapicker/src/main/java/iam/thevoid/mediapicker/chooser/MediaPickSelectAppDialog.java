package iam.thevoid.mediapicker.chooser;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

import iam.thevoid.mediapicker.R;
import iam.thevoid.mediapicker.util.IntentUtils;

public class MediaPickSelectAppDialog extends BottomSheetDialogFragment implements AdapterView.OnItemClickListener {

    public static final String EXTRA_RESOLVE = "EXTRA_RESOLVE";

    RecyclerView recyclerView;

    private ChooseAppAdapter adapter;

    private OnSelectAppCallback callback;

    ArrayList<IntentData> mParcelableArrayList;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.about_dialog, container, false);
        recyclerView = view.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setVerticalScrollBarEnabled(true);

        mParcelableArrayList = getIntentDatas(savedInstanceState);

        adapter = new ChooseAppAdapter(getActivity(), mParcelableArrayList);
        adapter.setOnItemClickListener(this);

        recyclerView.setAdapter(adapter);

        return view;
    }

    private ArrayList<IntentData> getIntentDatas(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey(EXTRA_RESOLVE)) {
            return savedInstanceState.getParcelableArrayList(EXTRA_RESOLVE);
        } else if (getArguments() != null && getArguments().containsKey(EXTRA_RESOLVE)) {
            return getArguments().getParcelableArrayList(EXTRA_RESOLVE);
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(EXTRA_RESOLVE, mParcelableArrayList);
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
            callback.onAppSelect(view.getContext(), adapter.getIntentData(position));
        }
    }


    public static void showForResult(Context context, List<IntentData> intents, OnSelectAppCallback callback) {
        show(IntentUtils.getFragmentActivity(context).getSupportFragmentManager(),
                intents,
                callback);
    }

    private static <T extends IntentData> void show(
            FragmentManager fm,
            List<T> data,
            OnSelectAppCallback callback) {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(EXTRA_RESOLVE, new ArrayList<>(data));
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

    public interface OnSelectAppCallback {
        void onAppSelect(Context context, IntentData intentData);
    }
}
