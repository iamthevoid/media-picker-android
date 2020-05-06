package iam.thevoid.mediapicker.rxmediapicker

import android.net.Uri

interface ImageReceiver {
    fun onDismiss()
    fun onImagePickFinish(uri: Uri?)
}