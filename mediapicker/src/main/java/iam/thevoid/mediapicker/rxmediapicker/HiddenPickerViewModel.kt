package iam.thevoid.mediapicker.rxmediapicker

import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import iam.thevoid.util.weak
import java.util.concurrent.Executors

class HiddenPickerViewModel : ViewModel() {

    var imageReceiver by weak<ImageReceiver>()

    private val executor by lazy { Executors.newSingleThreadExecutor() }

    fun fetchPhotoUriFromIntent(any: Any?) =
            executor.execute { onImagePicked(bitmapToUriConverter(any)) }

    fun onImagePicked(uri: Uri?) {
        imageReceiver?.onImagePickFinish(uri)
    }
}