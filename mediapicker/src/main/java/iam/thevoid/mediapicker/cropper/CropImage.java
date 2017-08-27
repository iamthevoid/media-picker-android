package iam.thevoid.mediapicker.cropper;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import iam.thevoid.mediapicker.R;

/**
 * Created by iam on 12/07/2017.
 */

public final class CropImage {
    public static final String CROP_IMAGE_EXTRA_SOURCE = "CROP_IMAGE_EXTRA_SOURCE";
    public static final String CROP_IMAGE_EXTRA_OPTIONS = "CROP_IMAGE_EXTRA_OPTIONS";
    public static final String CROP_IMAGE_EXTRA_RESULT = "CROP_IMAGE_EXTRA_RESULT";
    public static final int PICK_IMAGE_CHOOSER_REQUEST_CODE = 200;
    public static final int PICK_IMAGE_PERMISSIONS_REQUEST_CODE = 201;
    public static final int CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE = 2011;
    public static final int CROP_IMAGE_ACTIVITY_REQUEST_CODE = 203;
    public static final int CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE = 204;

    private CropImage() {
    }

    public static Bitmap toOvalBitmap(@NonNull Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        int color = -12434878;
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        RectF rect = new RectF(0.0F, 0.0F, (float) width, (float) height);
        canvas.drawOval(rect, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, 0.0F, 0.0F, paint);
        bitmap.recycle();
        return output;
    }

    public static void startPickImageActivity(@NonNull Activity activity) {
        activity.startActivityForResult(getPickImageChooserIntent(activity), 200);
    }

    public static Intent getPickImageChooserIntent(@NonNull Context context) {
        return getPickImageChooserIntent(context, context.getString(R.string.pick_image_intent_chooser_title), false, true);
    }

    public static Intent getPickImageChooserIntent(@NonNull Context context, CharSequence title, boolean includeDocuments, boolean includeCamera) {
        List<Intent> allIntents = new ArrayList();
        PackageManager packageManager = context.getPackageManager();
        if (!isExplicitCameraPermissionRequired(context) && includeCamera) {
            allIntents.addAll(getCameraIntents(context, packageManager));
        }

        List<Intent> galleryIntents = getGalleryIntents(packageManager, "android.intent.action.GET_CONTENT", includeDocuments);
        if (galleryIntents.size() == 0) {
            galleryIntents = getGalleryIntents(packageManager, "android.intent.action.PICK", includeDocuments);
        }

        allIntents.addAll(galleryIntents);
        Intent target;
        if (allIntents.isEmpty()) {
            target = new Intent();
        } else {
            target = (Intent) allIntents.get(allIntents.size() - 1);
            allIntents.remove(allIntents.size() - 1);
        }

        Intent chooserIntent = Intent.createChooser(target, title);
        chooserIntent.putExtra("android.intent.extra.INITIAL_INTENTS", (Parcelable[]) allIntents.toArray(new Parcelable[allIntents.size()]));
        return chooserIntent;
    }

    public static Intent getCameraIntent(@NonNull Context context, Uri outputFileUri) {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        if (outputFileUri == null) {
            outputFileUri = getCaptureImageOutputUri(context);
        }

        intent.putExtra("output", outputFileUri);
        return intent;
    }

    public static List<Intent> getCameraIntents(@NonNull Context context, @NonNull PackageManager packageManager) {
        List<Intent> allIntents = new ArrayList();
        Uri outputFileUri = getCaptureImageOutputUri(context);
        Intent captureIntent = new Intent("android.media.action.IMAGE_CAPTURE");
        List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);

        Intent intent;
        for (Iterator var6 = listCam.iterator(); var6.hasNext(); allIntents.add(intent)) {
            ResolveInfo res = (ResolveInfo) var6.next();
            intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            if (outputFileUri != null) {
                intent.putExtra("output", outputFileUri);
            }
        }

        return allIntents;
    }

    public static List<Intent> getGalleryIntents(@NonNull PackageManager packageManager, String action, boolean includeDocuments) {
        List<Intent> intents = new ArrayList();
        Intent galleryIntent = action == "android.intent.action.GET_CONTENT" ? new Intent(action) : new Intent(action, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");
        List<ResolveInfo> listGallery = packageManager.queryIntentActivities(galleryIntent, 0);
        Iterator var6 = listGallery.iterator();

        while (var6.hasNext()) {
            ResolveInfo res = (ResolveInfo) var6.next();
            Intent intent = new Intent(galleryIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            intents.add(intent);
        }

        if (!includeDocuments) {
            var6 = intents.iterator();

            while (var6.hasNext()) {
                Intent intent = (Intent) var6.next();
                if (intent.getComponent().getClassName().equals("com.android.documentsui.DocumentsActivity")) {
                    intents.remove(intent);
                    break;
                }
            }
        }

        return intents;
    }

    public static boolean isExplicitCameraPermissionRequired(@NonNull Context context) {
        return Build.VERSION.SDK_INT >= 23 && hasPermissionInManifest(context, "android.permission.CAMERA") && context.checkSelfPermission("android.permission.CAMERA") != 0;
    }

    public static boolean hasPermissionInManifest(@NonNull Context context, @NonNull String permissionName) {
        String packageName = context.getPackageName();

        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, 4096);
            String[] declaredPermisisons = packageInfo.requestedPermissions;
            if (declaredPermisisons != null && declaredPermisisons.length > 0) {
                String[] var5 = declaredPermisisons;
                int var6 = declaredPermisisons.length;

                for (int var7 = 0; var7 < var6; ++var7) {
                    String p = var5[var7];
                    if (p.equalsIgnoreCase(permissionName)) {
                        return true;
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException var9) {
            ;
        }

        return false;
    }

    public static Uri getCaptureImageOutputUri(@NonNull Context context) {
        Uri outputFileUri = null;
        File getImage = context.getExternalCacheDir();
        if (getImage != null) {
            outputFileUri = Uri.fromFile(new File(getImage.getPath(), "pickImageResult.jpeg"));
        }

        return outputFileUri;
    }

    public static Uri getPickImageResultUri(@NonNull Context context, @Nullable Intent data) {
        boolean isCamera = true;
        if (data != null && data.getData() != null) {
            String action = data.getAction();
            isCamera = action != null && action.equals("android.media.action.IMAGE_CAPTURE");
        }

        return !isCamera && data.getData() != null ? data.getData() : getCaptureImageOutputUri(context);
    }

    public static boolean isReadExternalStoragePermissionsRequired(@NonNull Context context, @NonNull Uri uri) {
        return Build.VERSION.SDK_INT >= 23 && context.checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") != 0 && isUriRequiresPermissions(context, uri);
    }

    public static boolean isUriRequiresPermissions(@NonNull Context context, @NonNull Uri uri) {
        try {
            ContentResolver resolver = context.getContentResolver();
            InputStream stream = resolver.openInputStream(uri);
            if (stream != null) {
                stream.close();
            }

            return false;
        } catch (Exception var4) {
            return true;
        }
    }

    public static ActivityBuilder activity() {
        return new ActivityBuilder((Uri) null);
    }

    public static ActivityBuilder activity(@Nullable Uri uri) {
        return new ActivityBuilder(uri);
    }

    public static ActivityResult getActivityResult(@Nullable Intent data) {
        return data != null ? (ActivityResult) data.getParcelableExtra("CROP_IMAGE_EXTRA_RESULT") : null;
    }

    public static final class ActivityResult extends CropImageView.CropResult implements Parcelable {
        public static final Creator<ActivityResult> CREATOR = new Creator<ActivityResult>() {
            public ActivityResult createFromParcel(Parcel in) {
                return new ActivityResult(in);
            }

            public ActivityResult[] newArray(int size) {
                return new ActivityResult[size];
            }
        };

        public ActivityResult(Uri originalUri, Uri uri, Exception error, float[] cropPoints, Rect cropRect, int rotation, int sampleSize) {
            super((Bitmap) null, originalUri, (Bitmap) null, uri, error, cropPoints, cropRect, rotation, sampleSize);
        }

        protected ActivityResult(Parcel in) {
            super((Bitmap) null, (Uri) in.readParcelable(Uri.class.getClassLoader()), (Bitmap) null, (Uri) in.readParcelable(Uri.class.getClassLoader()), (Exception) in.readSerializable(), in.createFloatArray(), (Rect) in.readParcelable(Rect.class.getClassLoader()), in.readInt(), in.readInt());
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(this.getOriginalUri(), flags);
            dest.writeParcelable(this.getUri(), flags);
            dest.writeSerializable(this.getError());
            dest.writeFloatArray(this.getCropPoints());
            dest.writeParcelable(this.getCropRect(), flags);
            dest.writeInt(this.getRotation());
            dest.writeInt(this.getSampleSize());
        }

        public int describeContents() {
            return 0;
        }
    }

    public static final class ActivityBuilder {
        @Nullable
        private final Uri mSource;
        private final CropImageOptions mOptions;

        private ActivityBuilder(@Nullable Uri source) {
            this.mSource = source;
            this.mOptions = new CropImageOptions();
        }

        public Intent getIntent(@NonNull Context context) {
            return this.getIntent(context, CropImageActivity.class);
        }

        public Intent getIntent(@NonNull Context context, @Nullable Class<?> cls) {
            this.mOptions.validate();
            Intent intent = new Intent();
            intent.setClass(context, cls);
            intent.putExtra("CROP_IMAGE_EXTRA_SOURCE", this.mSource);
            intent.putExtra("CROP_IMAGE_EXTRA_OPTIONS", this.mOptions);
            return intent;
        }

        public void start(@NonNull Activity activity) {
            this.mOptions.validate();
            activity.startActivityForResult(this.getIntent(activity), 203);
        }

        public void start(@NonNull Activity activity, @Nullable Class<?> cls) {
            this.mOptions.validate();
            activity.startActivityForResult(this.getIntent(activity, cls), 203);
        }

        public void start(@NonNull Context context, @NonNull Fragment fragment) {
            fragment.startActivityForResult(this.getIntent(context), 203);
        }

        @RequiresApi(
                api = 11
        )
        public void start(@NonNull Context context, @NonNull android.app.Fragment fragment) {
            fragment.startActivityForResult(this.getIntent(context), 203);
        }

        public void start(@NonNull Context context, @NonNull Fragment fragment, @Nullable Class<?> cls) {
            fragment.startActivityForResult(this.getIntent(context, cls), 203);
        }

        @RequiresApi(
                api = 11
        )
        public void start(@NonNull Context context, @NonNull android.app.Fragment fragment, @Nullable Class<?> cls) {
            fragment.startActivityForResult(this.getIntent(context, cls), 203);
        }

        public ActivityBuilder setCropShape(@NonNull CropImageView.CropShape cropShape) {
            this.mOptions.cropShape = cropShape;
            return this;
        }

        public ActivityBuilder setSnapRadius(float snapRadius) {
            this.mOptions.snapRadius = snapRadius;
            return this;
        }

        public ActivityBuilder setTouchRadius(float touchRadius) {
            this.mOptions.touchRadius = touchRadius;
            return this;
        }

        public ActivityBuilder setGuidelines(@NonNull CropImageView.Guidelines guidelines) {
            this.mOptions.guidelines = guidelines;
            return this;
        }

        public ActivityBuilder setScaleType(@NonNull CropImageView.ScaleType scaleType) {
            this.mOptions.scaleType = scaleType;
            return this;
        }

        public ActivityBuilder setShowCropOverlay(boolean showCropOverlay) {
            this.mOptions.showCropOverlay = showCropOverlay;
            return this;
        }

        public ActivityBuilder setAutoZoomEnabled(boolean autoZoomEnabled) {
            this.mOptions.autoZoomEnabled = autoZoomEnabled;
            return this;
        }

        public ActivityBuilder setMultiTouchEnabled(boolean multiTouchEnabled) {
            this.mOptions.multiTouchEnabled = multiTouchEnabled;
            return this;
        }

        public ActivityBuilder setMaxZoom(int maxZoom) {
            this.mOptions.maxZoom = maxZoom;
            return this;
        }

        public ActivityBuilder setInitialCropWindowPaddingRatio(float initialCropWindowPaddingRatio) {
            this.mOptions.initialCropWindowPaddingRatio = initialCropWindowPaddingRatio;
            return this;
        }

        public ActivityBuilder setFixAspectRatio(boolean fixAspectRatio) {
            this.mOptions.fixAspectRatio = fixAspectRatio;
            return this;
        }

        public ActivityBuilder setAspectRatio(int aspectRatioX, int aspectRatioY) {
            this.mOptions.aspectRatioX = aspectRatioX;
            this.mOptions.aspectRatioY = aspectRatioY;
            this.mOptions.fixAspectRatio = true;
            return this;
        }

        public ActivityBuilder setBorderLineThickness(float borderLineThickness) {
            this.mOptions.borderLineThickness = borderLineThickness;
            return this;
        }

        public ActivityBuilder setBorderLineColor(int borderLineColor) {
            this.mOptions.borderLineColor = borderLineColor;
            return this;
        }

        public ActivityBuilder setBorderCornerThickness(float borderCornerThickness) {
            this.mOptions.borderCornerThickness = borderCornerThickness;
            return this;
        }

        public ActivityBuilder setBorderCornerOffset(float borderCornerOffset) {
            this.mOptions.borderCornerOffset = borderCornerOffset;
            return this;
        }

        public ActivityBuilder setBorderCornerLength(float borderCornerLength) {
            this.mOptions.borderCornerLength = borderCornerLength;
            return this;
        }

        public ActivityBuilder setBorderCornerColor(int borderCornerColor) {
            this.mOptions.borderCornerColor = borderCornerColor;
            return this;
        }

        public ActivityBuilder setGuidelinesThickness(float guidelinesThickness) {
            this.mOptions.guidelinesThickness = guidelinesThickness;
            return this;
        }

        public ActivityBuilder setGuidelinesColor(int guidelinesColor) {
            this.mOptions.guidelinesColor = guidelinesColor;
            return this;
        }

        public ActivityBuilder setBackgroundColor(int backgroundColor) {
            this.mOptions.backgroundColor = backgroundColor;
            return this;
        }

        public ActivityBuilder setMinCropWindowSize(int minCropWindowWidth, int minCropWindowHeight) {
            this.mOptions.minCropWindowWidth = minCropWindowWidth;
            this.mOptions.minCropWindowHeight = minCropWindowHeight;
            return this;
        }

        public ActivityBuilder setMinCropResultSize(int minCropResultWidth, int minCropResultHeight) {
            this.mOptions.minCropResultWidth = minCropResultWidth;
            this.mOptions.minCropResultHeight = minCropResultHeight;
            return this;
        }

        public ActivityBuilder setMaxCropResultSize(int maxCropResultWidth, int maxCropResultHeight) {
            this.mOptions.maxCropResultWidth = maxCropResultWidth;
            this.mOptions.maxCropResultHeight = maxCropResultHeight;
            return this;
        }

        public ActivityBuilder setActivityTitle(String activityTitle) {
            this.mOptions.activityTitle = activityTitle;
            return this;
        }

        public ActivityBuilder setActivityMenuIconColor(int activityMenuIconColor) {
            this.mOptions.activityMenuIconColor = activityMenuIconColor;
            return this;
        }

        public ActivityBuilder setOutputUri(Uri outputUri) {
            this.mOptions.outputUri = outputUri;
            return this;
        }

        public ActivityBuilder setOutputCompressFormat(Bitmap.CompressFormat outputCompressFormat) {
            this.mOptions.outputCompressFormat = outputCompressFormat;
            return this;
        }

        public ActivityBuilder setOutputCompressQuality(int outputCompressQuality) {
            this.mOptions.outputCompressQuality = outputCompressQuality;
            return this;
        }

        public ActivityBuilder setRequestedSize(int reqWidth, int reqHeight) {
            return this.setRequestedSize(reqWidth, reqHeight, CropImageView.RequestSizeOptions.RESIZE_INSIDE);
        }

        public ActivityBuilder setRequestedSize(int reqWidth, int reqHeight, CropImageView.RequestSizeOptions options) {
            this.mOptions.outputRequestWidth = reqWidth;
            this.mOptions.outputRequestHeight = reqHeight;
            this.mOptions.outputRequestSizeOptions = options;
            return this;
        }

        public ActivityBuilder setNoOutputImage(boolean noOutputImage) {
            this.mOptions.noOutputImage = noOutputImage;
            return this;
        }

        public ActivityBuilder setInitialCropWindowRectangle(Rect initialCropWindowRectangle) {
            this.mOptions.initialCropWindowRectangle = initialCropWindowRectangle;
            return this;
        }

        public ActivityBuilder setInitialRotation(int initialRotation) {
            this.mOptions.initialRotation = (initialRotation + 360) % 360;
            return this;
        }

        public ActivityBuilder setAllowRotation(boolean allowRotation) {
            this.mOptions.allowRotation = allowRotation;
            return this;
        }

        public ActivityBuilder setAllowFlipping(boolean allowFlipping) {
            this.mOptions.allowFlipping = allowFlipping;
            return this;
        }

        public ActivityBuilder setAllowCounterRotation(boolean allowCounterRotation) {
            this.mOptions.allowCounterRotation = allowCounterRotation;
            return this;
        }

        public ActivityBuilder setRotationDegrees(int rotationDegrees) {
            this.mOptions.rotationDegrees = (rotationDegrees + 360) % 360;
            return this;
        }

        public ActivityBuilder setFlipHorizontally(boolean flipHorizontally) {
            this.mOptions.flipHorizontally = flipHorizontally;
            return this;
        }

        public ActivityBuilder setFlipVertically(boolean flipVertically) {
            this.mOptions.flipVertically = flipVertically;
            return this;
        }
    }
}
