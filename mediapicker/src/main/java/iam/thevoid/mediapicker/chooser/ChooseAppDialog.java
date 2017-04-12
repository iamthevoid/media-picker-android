package iam.thevoid.mediapicker.chooser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import java.util.ArrayList;

import iam.thevoid.mediapicker.util.ResAdapter;
import iam.thevoid.mediapicker.R;

/**
 * Created by iam on 03.04.17.
 */

public class ChooseAppDialog extends BottomSheetDialogFragment implements AdapterView.OnItemClickListener {

    public static final String EXTRA_TITLE = "EXTRA_TITLE";
    public static final String EXTRA_RESOLVE = "EXTRA_RESOLVE";

    TextView title;
    RecyclerView recyclerView;

    ArrayList<IntentData> parcelableArrayList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.about_dialog, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler);
        this.title = (TextView) view.findViewById(R.id.title);

        String title = getArguments().getString(EXTRA_TITLE);
        if (title != null) {
            this.title.setText(title);
        } else {
            this.title.setVisibility(View.GONE);
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        recyclerView.setVerticalScrollBarEnabled(true);

        parcelableArrayList = getArguments().getParcelableArrayList(EXTRA_RESOLVE);


        ChooseAppAdapter adapter = new ChooseAppAdapter(getActivity(), parcelableArrayList);

        adapter.setOnItemClickListener(this);

        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        dismiss();
        IntentData data = parcelableArrayList.get(position);
        if (data.getRequestCode() == -1) {
            getActivity().startActivity(data.getIntent());
        } else {
            getActivity().startActivityForResult(data.getIntent(), data.getRequestCode());
        }
    }


    public static void showForResult(Activity context, android.support.v4.app.FragmentManager fragmentManager, int title, IntentData... intents) {
        show(fragmentManager, ResAdapter.getString(context, title), makeList(intents));
    }

    private static <T extends IntentData> void show(android.support.v4.app.FragmentManager fm,
                                                    String title,
                                                    ArrayList<T> data) {
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_TITLE, title);
        bundle.putParcelableArrayList(EXTRA_RESOLVE, data);
        ChooseAppDialog fragment = new ChooseAppDialog();
        fragment.setArguments(bundle);
        fragment.show(fm);
    }

    public void show(android.support.v4.app.FragmentManager fragmentManager) {
        super.show(fragmentManager, this.getClass().getCanonicalName());
    }

    private static ArrayList<IntentData> makeList(IntentData... intentDatas) {
        ArrayList<IntentData> ids = new ArrayList<>();
        for (IntentData id : intentDatas) {
            ids.add(id);
        }
        return ids;
    }

    private static ArrayList<IntentData> makeShowIntents(Intent[] intents) {
        if (intents == null || intents.length == 0) {
            return new ArrayList<>();
        }

        ArrayList<IntentData> intentDatas = new ArrayList<>();

        for (Intent i : intents) {
            intentDatas.add(new IntentData(i, -1));
        }

        return intentDatas;
    }
}
