package iam.thevoid.mediapicker.picker.fragment

import android.net.Uri
import androidx.lifecycle.ViewModel
import iam.thevoid.mediapicker.picker.ImageReceiver
import iam.thevoid.mediapicker.picker.bitmapToUriConverter
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