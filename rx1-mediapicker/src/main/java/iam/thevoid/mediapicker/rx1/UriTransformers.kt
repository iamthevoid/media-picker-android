@file:JvmName(TAG)

package iam.thevoid.mediapicker.rx1

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import iam.thevoid.mediapicker.picker.filepath
import iam.thevoid.mediapicker.picker.toBitmap
import iam.thevoid.mediapicker.picker.toFile
import iam.thevoid.mediapicker.util.FileUtil
import rx.Emitter
import rx.Observable
import rx.schedulers.Schedulers
import java.io.File

private const val TAG = "UriTransformers";

fun bitmap(context: Context) =
        Observable.Transformer<Uri, Bitmap> {
            it.flatMap { uri: Uri -> uri.toObservable { toBitmap(context) } }
                    .subscribeOn(Schedulers.computation())
        }

fun filepath(context: Context) =
        Observable.Transformer<Uri, String> {
            it.flatMap { uri: Uri ->
                FileUtil.getPath(context, uri)?.let { Observable.just(it) }
                        ?: uri.toObservable { toFile(context, File(uri.filepath(context))).absolutePath }
            }.subscribeOn(Schedulers.computation())
        }

@JvmOverloads
fun file(context: Context, path: String? = null): Observable.Transformer<Uri, File> {
    return Observable.Transformer { uriObservable: Observable<Uri> ->
        uriObservable
                .flatMap { uri: Uri ->
                    FileUtil.getPath(context, uri)?.let(::File)?.let { Observable.just(it) }
                            ?: uri.toObservable { toFile(context, path?.let(::File)) }
                }.subscribeOn(Schedulers.computation())
    }
}

private fun <T> Uri.toObservable(convert: Uri.() -> T) =
        Observable.create<T>({
            try {
                it.onNext(convert())
                it.onCompleted()
            } catch (e: Exception) {
                Log.e(TAG, "Error converting uri", e)
                it.onError(e)
            }
        }, Emitter.BackpressureMode.BUFFER)
                .subscribeOn(Schedulers.computation())