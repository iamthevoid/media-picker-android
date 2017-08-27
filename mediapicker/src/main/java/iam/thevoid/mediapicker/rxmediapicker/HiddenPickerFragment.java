package iam.thevoid.mediapicker.rxmediapicker;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

import iam.thevoid.mediapicker.cropper.CropArea;
import iam.thevoid.mediapicker.util.FileUtil;
import rx.Emitter;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

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
                if (data.getData() != null) {
                    uri = data.getData();
                    onImagePicked();
                    popBackStack();
                } else if (requestCode == Purpose.REQUEST_TAKE_PHOTO) {
                    if (data.getExtras() != null && data.getExtras().get("data") != null) {
                        Observable.<Uri>create(emitter -> {
                            emitter.onNext(bitmapToUriConverter(data.getExtras().get("data")));
                            emitter.onCompleted();
                        }, Emitter.BackpressureMode.NONE)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(u -> {
                                    uri = u;
                                    onImagePicked();
                                    popBackStack();
                                });
                    }
                }
            }
        }
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

//    private Uri bitmapToUriConverter(Object mBitmap) {
//
//        if (!(mBitmap instanceof Bitmap)) {
//            return null;
//        }
//
//        Uri uri = null;
//        try {
//            final BitmapFactory.Options options = new BitmapFactory.Options();
//            // Calculate inSampleSize
//            options.inSampleSize = calculateInSampleSize(options, 100, 100);
//
//            // Decode bitmap with inSampleSize set
//            options.inJustDecodeBounds = false;
//            Bitmap newBitmap = Bitmap.createScaledBitmap((Bitmap) mBitmap, 200, 200,
//                    true);
//            File file = new File(getActivity().getFilesDir(), "Image"
//                    + new Random().nextInt() + ".jpeg");
//            FileOutputStream out = getActivity().openFileOutput(file.getName(),
//                    Context.MODE_WORLD_READABLE);
//            newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
//            out.flush();
//            out.close();
//            //get absolute path
//            String realPath = file.getAbsolutePath();
//            File f = new File(realPath);
//            uri = Uri.fromFile(f);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            Log.e("Your Error Message", e.getMessage());
//        }
//        return uri;
//    }

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
