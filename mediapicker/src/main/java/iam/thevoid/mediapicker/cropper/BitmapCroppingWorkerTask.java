package iam.thevoid.mediapicker.cropper;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

/**
 * Created by iam on 12/07/2017.
 */

final class BitmapCroppingWorkerTask extends AsyncTask<Void, Void, BitmapCroppingWorkerTask.Result> {
    private final WeakReference<CropImageView> mCropImageViewReference;
    private final Bitmap mBitmap;
    private final Uri mUri;
    private final Context mContext;
    private final float[] mCropPoints;
    private final int mDegreesRotated;
    private final int mOrgWidth;
    private final int mOrgHeight;
    private final boolean mFixAspectRatio;
    private final int mAspectRatioX;
    private final int mAspectRatioY;
    private final int mReqWidth;
    private final int mReqHeight;
    private final boolean mFlipHorizontally;
    private final boolean mFlipVertically;
    private final CropImageView.RequestSizeOptions mReqSizeOptions;
    private final Uri mSaveUri;
    private final Bitmap.CompressFormat mSaveCompressFormat;
    private final int mSaveCompressQuality;

    BitmapCroppingWorkerTask(CropImageView cropImageView, Bitmap bitmap, float[] cropPoints, int degreesRotated, boolean fixAspectRatio, int aspectRatioX, int aspectRatioY, int reqWidth, int reqHeight, boolean flipHorizontally, boolean flipVertically, CropImageView.RequestSizeOptions options, Uri saveUri, Bitmap.CompressFormat saveCompressFormat, int saveCompressQuality) {
        this.mCropImageViewReference = new WeakReference(cropImageView);
        this.mContext = cropImageView.getContext();
        this.mBitmap = bitmap;
        this.mCropPoints = cropPoints;
        this.mUri = null;
        this.mDegreesRotated = degreesRotated;
        this.mFixAspectRatio = fixAspectRatio;
        this.mAspectRatioX = aspectRatioX;
        this.mAspectRatioY = aspectRatioY;
        this.mReqWidth = reqWidth;
        this.mReqHeight = reqHeight;
        this.mFlipHorizontally = flipHorizontally;
        this.mFlipVertically = flipVertically;
        this.mReqSizeOptions = options;
        this.mSaveUri = saveUri;
        this.mSaveCompressFormat = saveCompressFormat;
        this.mSaveCompressQuality = saveCompressQuality;
        this.mOrgWidth = 0;
        this.mOrgHeight = 0;
    }

    BitmapCroppingWorkerTask(CropImageView cropImageView, Uri uri, float[] cropPoints, int degreesRotated, int orgWidth, int orgHeight, boolean fixAspectRatio, int aspectRatioX, int aspectRatioY, int reqWidth, int reqHeight, boolean flipHorizontally, boolean flipVertically, CropImageView.RequestSizeOptions options, Uri saveUri, Bitmap.CompressFormat saveCompressFormat, int saveCompressQuality) {
        this.mCropImageViewReference = new WeakReference(cropImageView);
        this.mContext = cropImageView.getContext();
        this.mUri = uri;
        this.mCropPoints = cropPoints;
        this.mDegreesRotated = degreesRotated;
        this.mFixAspectRatio = fixAspectRatio;
        this.mAspectRatioX = aspectRatioX;
        this.mAspectRatioY = aspectRatioY;
        this.mOrgWidth = orgWidth;
        this.mOrgHeight = orgHeight;
        this.mReqWidth = reqWidth;
        this.mReqHeight = reqHeight;
        this.mFlipHorizontally = flipHorizontally;
        this.mFlipVertically = flipVertically;
        this.mReqSizeOptions = options;
        this.mSaveUri = saveUri;
        this.mSaveCompressFormat = saveCompressFormat;
        this.mSaveCompressQuality = saveCompressQuality;
        this.mBitmap = null;
    }

    public Uri getUri() {
        return this.mUri;
    }

    protected Result doInBackground(Void... params) {
        try {
            if (!this.isCancelled()) {
                BitmapUtils.BitmapSampled bitmapSampled;
                if (this.mUri != null) {
                    bitmapSampled = BitmapUtils.cropBitmap(this.mContext, this.mUri, this.mCropPoints, this.mDegreesRotated, this.mOrgWidth, this.mOrgHeight, this.mFixAspectRatio, this.mAspectRatioX, this.mAspectRatioY, this.mReqWidth, this.mReqHeight, this.mFlipHorizontally, this.mFlipVertically);
                } else {
                    if (this.mBitmap == null) {
                        return new Result((Bitmap) null, 1);
                    }

                    bitmapSampled = BitmapUtils.cropBitmapObjectHandleOOM(this.mBitmap, this.mCropPoints, this.mDegreesRotated, this.mFixAspectRatio, this.mAspectRatioX, this.mAspectRatioY, this.mFlipHorizontally, this.mFlipVertically);
                }

                Bitmap bitmap = BitmapUtils.resizeBitmap(bitmapSampled.bitmap, this.mReqWidth, this.mReqHeight, this.mReqSizeOptions);
                if (this.mSaveUri == null) {
                    return new Result(bitmap, bitmapSampled.sampleSize);
                } else {
                    BitmapUtils.writeBitmapToUri(this.mContext, bitmap, this.mSaveUri, this.mSaveCompressFormat, this.mSaveCompressQuality);
                    if (bitmap != null) {
                        bitmap.recycle();
                    }

                    return new Result(this.mSaveUri, bitmapSampled.sampleSize);
                }
            } else {
                return null;
            }
        } catch (Exception var4) {
            return new Result(var4, this.mSaveUri != null);
        }
    }

    protected void onPostExecute(Result result) {
        if (result != null) {
            boolean completeCalled = false;
            if (!this.isCancelled()) {
                CropImageView cropImageView = (CropImageView) this.mCropImageViewReference.get();
                if (cropImageView != null) {
                    completeCalled = true;
                    cropImageView.onImageCroppingAsyncComplete(result);
                }
            }

            if (!completeCalled && result.bitmap != null) {
                result.bitmap.recycle();
            }
        }

    }

    static final class Result {
        public final Bitmap bitmap;
        public final Uri uri;
        final Exception error;
        final boolean isSave;
        final int sampleSize;

        Result(Bitmap bitmap, int sampleSize) {
            this.bitmap = bitmap;
            this.uri = null;
            this.error = null;
            this.isSave = false;
            this.sampleSize = sampleSize;
        }

        Result(Uri uri, int sampleSize) {
            this.bitmap = null;
            this.uri = uri;
            this.error = null;
            this.isSave = true;
            this.sampleSize = sampleSize;
        }

        Result(Exception error, boolean isSave) {
            this.bitmap = null;
            this.uri = null;
            this.error = error;
            this.isSave = isSave;
            this.sampleSize = 1;
        }
    }
}