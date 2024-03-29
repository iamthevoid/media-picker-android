@file:JvmName(TAG)

package iam.thevoid.mediapicker.rx3

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import iam.thevoid.mediapicker.picker.filepath
import iam.thevoid.mediapicker.picker.toBitmap
import iam.thevoid.mediapicker.picker.toFile
import iam.thevoid.mediapicker.util.FileUtil
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.MaybeTransformer
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File

private const val TAG = "UriTransformers";

fun bitmap(context: Context) =
        MaybeTransformer<Uri, Bitmap> {
            it.flatMap { uri: Uri -> uri.toMaybe { toBitmap(context) } }
                    .subscribeOn(Schedulers.computation())
        }

fun filepath(context: Context) =
        MaybeTransformer<Uri, String> {
            it.flatMap { uri: Uri ->
                FileUtil.getPath(context, uri)?.let { Maybe.just(it) }
                        ?: uri.toMaybe { toFile(context, File(uri.filepath(context))).absolutePath }
            }.subscribeOn(Schedulers.computation())
        }

@JvmOverloads
fun file(context: Context, path: String? = null): MaybeTransformer<Uri, File> {
    return MaybeTransformer {
        it.flatMap { uri: Uri ->
            FileUtil.getPath(context, uri)?.let(::File)?.let { Maybe.just(it) }
                ?: uri.toMaybe { toFile(context, path?.let(::File)) }
        }.subscribeOn(Schedulers.computation())
    }
}

@JvmOverloads
fun copyFileToAppDir(context: Context): MaybeTransformer<Uri, File> {
    return MaybeTransformer { uriObservable: Maybe<Uri> ->
        uriObservable
            .flatMap { uri: Uri ->
                Maybe.fromCallable<File> { FileUtil.copyFileToAppDir(context, uri) }
            }.subscribeOn(Schedulers.computation())
    }
}

private fun <T> Uri.toMaybe(convert: Uri.() -> T) =
    Maybe.create<T> {
        try {
            it.onSuccess(convert())
        } catch (e: Exception) {
            Log.d(TAG, "Error converting uri", e)
            it.onError(e)
        }
    }.subscribeOn(Schedulers.computation())