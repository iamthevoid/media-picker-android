package iam.thevoid.mediapicker.bus

import android.net.Uri
import iam.thevoid.mediapicker.rxmediapicker.Picker

object MediaPickerBus {
    private var picker: Picker<*>? = null

    fun attachMediaPicker(picker: Picker<*>) {
        MediaPickerBus.picker = picker
    }

    fun onImagePicked(uri: Uri?) {
        picker?.onImagePicked(uri)
    }

    fun onDismiss() {
        picker?.onDismiss()
    }
}