package iam.thevoid.mediapicker.rxmediapicker;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import iam.thevoid.mediapicker.util.Editor;
import iam.thevoid.mediapicker.util.FileUtil;
import rx.Emitter;
import rx.Observable;
import rx.exceptions.Exceptions;
import rx.schedulers.Schedulers;

/**
 * Created by iam on 14/08/2017.
 */

public final class UriTransformer {

    private static final String TAG = UriTransformer.class.getSimpleName();

    private UriTransformer() {
    }

    public static Observable.Transformer<Uri, Bitmap> bitmap(Context context) {
        return uriObservable ->
                uriObservable.flatMap(uri -> uriToBitmap(context, uri));
    }

    public static Observable.Transformer<Uri, String> filepath(Context context) {
        return uriObservable ->
                uriObservable
                        .flatMap(uri -> {
                            try {
                                String path;
                                if ((path = FileUtil.getPath(context, uri)) != null) {
                                    return Observable.just(path);
                                } else {
                                    String filepath = FileUtil.temp(context) + "/" +
                                            Editor.currentDateFilename(filenamePrefix(context, uri), FileUtil.extension(context, uri));
                                    return uriToFilepath(context, uri, new File(filepath));
                                }
                            } catch (Exception e) {
                                throw Exceptions.propagate(e);
                            }
                        });
    }

    public static Observable.Transformer<Uri, File> file(Context context) {
        return uriObservable ->
                uriObservable
                        .flatMap(uri -> {
                            try {
                                String path;
                                if ((path = FileUtil.getPath(context, uri)) != null) {
                                    return Observable.just(new File(path));
                                } else {
                                    String filepath = FileUtil.temp(context) + "/" +
                                            Editor.currentDateFilename(filenamePrefix(context, uri), FileUtil.extension(context, uri));
                                    return uriToFile(context, uri, new File(filepath));
                                }
                            } catch (Exception e) {
                                throw Exceptions.propagate(e);
                            }
                        });
    }

    public static Observable.Transformer<Uri, File> file(Context context, String path) {
        return uriObservable ->
                uriObservable
                        .subscribeOn(Schedulers.io())
                        .flatMap(uri -> {
                            try {
                                String localPath;
                                if ((localPath = FileUtil.getPath(context, uri)) != null) {
                                    return Observable.just(new File(localPath));
                                } else {
                                    return uriToFile(context, uri, new File(path));
                                }
                            } catch (Exception e) {
                                throw Exceptions.propagate(e);
                            }
                        });
    }

    public static Observable<File> uriToFile(final Context context, final Uri uri, final File file) {
        return Observable.create(fileEmitter -> {
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(uri);
                copyInputStreamToFile(inputStream, file);
                fileEmitter.onNext(file);
                fileEmitter.onCompleted();
            } catch (Exception e) {
                Log.e(TAG, "Error converting uri", e);
                fileEmitter.onError(e);
            }
        }, Emitter.BackpressureMode.NONE);
    }

    private static Observable<String> uriToFilepath(final Context context, final Uri uri, final File file) {
        return Observable.create(fileEmitter -> {
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(uri);
                copyInputStreamToFile(inputStream, file);
                fileEmitter.onNext(file.getAbsolutePath());
                fileEmitter.onCompleted();
            } catch (Exception e) {
                Log.e(TAG, "Error converting uri", e);
                fileEmitter.onError(e);
            }
        }, Emitter.BackpressureMode.NONE);
    }

    private static Observable<Bitmap> uriToBitmap(final Context context, final Uri uri) {
        return Observable.create(bitmapEmitter -> {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
                bitmapEmitter.onNext(bitmap);
                bitmapEmitter.onCompleted();
            } catch (IOException e) {
                Log.e(TAG, "Error converting uri", e);
                bitmapEmitter.onError(e);
            }
        }, Emitter.BackpressureMode.NONE);
    }

    private static void copyInputStreamToFile(InputStream in, File file) throws IOException {
        if (!file.exists()) {
            new File(file.getParent()).mkdirs();
            file.createNewFile();
        }
        OutputStream out = new FileOutputStream(file);
        byte[] buf = new byte[10 * 1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        out.close();
        in.close();
    }

    private static String filenamePrefix(Context context, Uri uri) {
        String ext = FileUtil.extension(context, uri);

        if (FileUtil.isVideoExt(ext)) {
            return "video";
        }

        if (FileUtil.isGifExt(ext)) {
            return "anim";
        }

        return "image";
    }

}
