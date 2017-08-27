package iam.thevoid.mediapicker.cropper;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.DisplayMetrics;

import java.lang.ref.WeakReference;

/**
 * Created by iam on 12/07/2017.
 */

final class BitmapLoadingWorkerTask extends AsyncTask<Void, Void, BitmapLoadingWorkerTask.Result> {
    private final WeakReference<CropImageView> mCropImageViewReference;
    private final Uri mUri;
    private final Context mContext;
    private final int mWidth;
    private final int mHeight;

    public BitmapLoadingWorkerTask(CropImageView cropImageView, Uri uri) {
        this.mUri = uri;
        this.mCropImageViewReference = new WeakReference(cropImageView);
        this.mContext = cropImageView.getContext();
        DisplayMetrics metrics = cropImageView.getResources().getDisplayMetrics();
        double densityAdj = metrics.density > 1.0F ? (double) (1.0F / metrics.density) : 1.0D;
        this.mWidth = (int) ((double) metrics.widthPixels * densityAdj);
        this.mHeight = (int) ((double) metrics.heightPixels * densityAdj);
    }

    public Uri getUri() {
        return this.mUri;
    }

    protected Result doInBackground(Void... params) {
        try {
            if (!this.isCancelled()) {
                BitmapUtils.BitmapSampled decodeResult = BitmapUtils.decodeSampledBitmap(this.mContext, this.mUri, this.mWidth, this.mHeight);
                if (!this.isCancelled()) {
                    BitmapUtils.RotateBitmapResult rotateResult = BitmapUtils.rotateBitmapByExif(decodeResult.bitmap, this.mContext, this.mUri);
                    return new Result(this.mUri, rotateResult.bitmap, decodeResult.sampleSize, rotateResult.degrees);
                }
            }

            return null;
        } catch (Exception var4) {
            return new Result(this.mUri, var4);
        }
    }

    protected void onPostExecute(Result result) {
        if (result != null) {
            boolean completeCalled = false;
            if (!this.isCancelled()) {
                CropImageView cropImageView = (CropImageView) this.mCropImageViewReference.get();
                if (cropImageView != null) {
                    completeCalled = true;
                    cropImageView.onSetImageUriAsyncComplete(result);
                }
            }

            if (!completeCalled && result.bitmap != null) {
                result.bitmap.recycle();
            }
        }

    }

    public static final class Result {
        public final Uri uri;
        public final Bitmap bitmap;
        public final int loadSampleSize;
        public final int degreesRotated;
        public final Exception error;

        Result(Uri uri, Bitmap bitmap, int loadSampleSize, int degreesRotated) {
            this.uri = uri;
            this.bitmap = bitmap;
            this.loadSampleSize = loadSampleSize;
            this.degreesRotated = degreesRotated;
            this.error = null;
        }

        Result(Uri uri, Exception error) {
            this.uri = uri;
            this.bitmap = null;
            this.loadSampleSize = 0;
            this.degreesRotated = 0;
            this.error = error;
        }
    }
}