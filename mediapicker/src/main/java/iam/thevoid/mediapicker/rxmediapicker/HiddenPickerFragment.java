package iam.thevoid.mediapicker.rxmediapicker;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import iam.thevoid.mediapicker.cropper.CropArea;
import iam.thevoid.mediapicker.util.FileUtil;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
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
            popBackStack();
        } else {
            if (resultCode == Activity.RESULT_OK) {
                if (data == null || requestCode == Purpose.REQUEST_TAKE_PHOTO && FileUtil.getPhotoPath(getActivity()).length() > 0) {
                    fetchPhotoUriFromPath();
                } else if (data != null) {
                    if (data.getData() != null) {
                        fetchImageUri(data);
                    } else if (requestCode == Purpose.REQUEST_TAKE_PHOTO) {
                        fetchPhotoUriFromIntent(data);
                    }
                } else {
                    popBackStack();
                }
            } else {
                popBackStack();
            }
        }
    }

    private void fetchImageUri(Intent data) {
        uri = data.getData();
        onImagePicked();
        popBackStack();
    }

    private void fetchPhotoUriFromIntent(Intent data) {
        if (data.getExtras() != null && data.getExtras().get("data") != null) {
            Observable.just(bitmapToUriConverter(data.getExtras().get("data")))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(u -> {
                        uri = u;
                        onImagePicked();
                        popBackStack();
                    });
        } else {
            popBackStack();
        }
    }

    private void fetchPhotoUriFromPath() {
        String pathname = FileUtil.generatePathForPhotoIntent(getActivity());
        uri = pathname == null ? Uri.parse("") : Uri.fromFile(new File(pathname));
        onImagePicked();
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

    private Uri bitmapToUriConverter(Object mBitmap) {

        if (!(mBitmap instanceof Bitmap)) {
            return null;
        }

        File tempFile = null;
        File tempDir = Environment.getExternalStorageDirectory();
        tempDir = new File(tempDir.getAbsolutePath() + "/.temp/");
        tempDir.mkdir();
        try {

            tempFile = File.createTempFile("Image"
                    + new Random().nextInt(), ".jpg", tempDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ((Bitmap) mBitmap).compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        byte[] bitmapData = bytes.toByteArray();

        if (tempFile == null) {
            // todo Unpredictable behaviour, try other way
            return null;
        }

        //write the bytes in file

        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(bitmapData);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Uri.fromFile(tempFile);
    }
}
