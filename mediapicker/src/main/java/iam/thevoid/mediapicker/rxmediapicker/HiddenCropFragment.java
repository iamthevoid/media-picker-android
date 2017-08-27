package iam.thevoid.mediapicker.rxmediapicker;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import iam.thevoid.mediapicker.cropper.CropArea;
import iam.thevoid.mediapicker.cropper.CropImage;
/**
 * Created by iam on 04/08/2017.
 */

public class HiddenCropFragment extends Fragment {

    private CropArea cropArea;
    private Uri uri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            handleIntent(getArguments());
        }
    }

    public static Fragment getFragment(CropArea cropArea, Uri uri) {
        Fragment fragment = new HiddenCropFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(RxMediaPicker.EXTRA_CROP_AREA, cropArea);
        bundle.putParcelable(RxMediaPicker.EXTRA_URI, uri);
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
        if (resultCode == Activity.RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            RxMediaPicker.instance().onImagePicked(result.getUri(), true);
        } else if (resultCode == Activity.RESULT_CANCELED) {
            RxMediaPicker.instance().dismiss();
        }
        popBackStack();
    }

    private void popBackStack() {
        FragmentManager fragmentManager = getActivity().getFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(HiddenCropFragment.class.getCanonicalName());
        if (fragment != null) {
            fragmentManager
                    .beginTransaction()
                    .remove(fragment)
                    .commitAllowingStateLoss();
        }
    }

    private void handleIntent(Bundle bundle) {
        cropArea = bundle.getParcelable(RxMediaPicker.EXTRA_CROP_AREA);
        uri = bundle.getParcelable(RxMediaPicker.EXTRA_URI);

        CropImage.ActivityBuilder builder = CropImage.activity(uri);
        if (cropArea.isDeterminate()) {
            builder
                    .setCropShape(cropArea.getCropShape())
                    .setAspectRatio(cropArea.getWidthRatio(), cropArea.getHeightRatio());
        } else {
            builder.setFixAspectRatio(false);
        }

        builder.start(getActivity(), this);
    }
}
