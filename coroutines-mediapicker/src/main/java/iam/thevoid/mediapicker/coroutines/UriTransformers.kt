@file:JvmName(TAG)
@file:OptIn(FlowPreview::class)

package iam.thevoid.mediapicker.coroutines

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import iam.thevoid.mediapicker.picker.filepath
import iam.thevoid.mediapicker.picker.toBitmap
import iam.thevoid.mediapicker.picker.toFile
import iam.thevoid.mediapicker.util.FileUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import java.io.File

private const val TAG = "UriTransformers";


fun bitmap(context: Context): Flow<Uri>.() -> Flow<Bitmap> = {
    flatMapConcat { uri: Uri -> uri.toFlow { toBitmap(context) } }
            .flowOn(Dispatchers.IO)
}

fun filepath(context: Context): Flow<Uri>.() -> Flow<String> = {
    flatMapConcat { uri: Uri ->
        FileUtil.getPath(context, uri)?.let { flowOf(it) }
                ?: uri.toFlow { toFile(context, File(uri.filepath(context))).absolutePath }
    }.flowOn(Dispatchers.IO)
}


@Deprecated("Not compatible with android 10+")
@JvmOverloads
fun file(context: Context): Flow<Uri>.() -> Flow<File> = {
    flatMapConcat { uri: Uri ->
        FileUtil.getPath(context, uri)?.let(::File)?.let { flowOf(it) }
            ?: uri.toFlow { toFile(context, path?.let(::File)) }
    }.flowOn(Dispatchers.IO)
}


@JvmOverloads
fun copyToAppDir(context: Context): Flow<Uri>.() -> Flow<File?> = {
    flatMapConcat { uri: Uri -> flowOf(FileUtil.copyFileToAppDir(context, uri)) }
        .flowOn(Dispatchers.IO)
}

private fun <T> Uri.toFlow(convert: Uri.() -> T) =
    flow { emit(convert()) }
