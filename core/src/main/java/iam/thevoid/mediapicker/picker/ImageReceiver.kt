package iam.thevoid.mediapicker.picker

import android.net.Uri

interface ImageReceiver {
    fun onDismiss()
    fun onImagePickFinish(uri: Uri?)
}