package iam.thevoid.mediapicker.rxmediapicker;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import iam.thevoid.mediapicker.cropper.CropArea;
import iam.thevoid.mediapicker.util.FileUtil;

/**
 * Created by iam on 04/08/2017.
 */

public class HiddenPickerFragment extends Fragment {

    private CropArea cropArea;
    private Uri uri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            handleIntent(getArguments());
        }
    }

    public static Fragment getFragment(Intent intent, CropArea cropArea, int requestCode) {
        Fragment fragment = new HiddenPickerFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(RxMediaPicker.EXTRA_INTENT, intent);
        bundle.putParcelable(RxMediaPicker.EXTRA_CROP_AREA, cropArea);
        bundle.putInt(RxMediaPicker.EXTRA_REQUEST_CODE, requestCode);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(RxMediaPicker.EXTRA_CROP_AREA, cropArea);
        outState.putParcelable(RxMediaPicker.EXTRA_URI, uri);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        cropArea = savedInstanceState.getParcelable(RxMediaPicker.EXTRA_CROP_AREA);
        uri = savedInstanceState.getParcelable(RxMediaPicker.EXTRA_URI);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_CANCELED) {
            RxMediaPicker.instance().dismiss();
        } else {
            if (resultCode == Activity.RESULT_OK) {
                uri = data.getData();
            }
            onImagePicked();
        }
        popBackStack();
    }

    private void popBackStack() {
        FragmentManager fragmentManager = getActivity().getFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(HiddenPickerFragment.class.getCanonicalName());
        if (fragment != null) {
            fragmentManager
                    .beginTransaction()
                    .remove(fragment)
                    .commitAllowingStateLoss();
        }
    }

    private void onImagePicked() {
        RxMediaPicker.instance().onImagePicked(uri,
                cropArea == null ||
                        (FileUtil.isVideoExt(FileUtil.extension(getActivity(), uri)) ||
                                FileUtil.isGifExt(FileUtil.extension(getActivity(), uri))));
    }

    private void handleIntent(Bundle bundle) {
        cropArea = bundle.getParcelable(RxMediaPicker.EXTRA_CROP_AREA);

        startActivityForResult(bundle.getParcelable(RxMediaPicker.EXTRA_INTENT),
                bundle.getInt(RxMediaPicker.EXTRA_REQUEST_CODE, 0));
    }
}
