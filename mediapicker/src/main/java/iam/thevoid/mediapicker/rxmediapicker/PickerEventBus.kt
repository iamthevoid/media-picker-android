package iam.thevoid.mediapicker.rxmediapicker

import android.net.Uri

object PickerEventBus {
    private var picker : Picker<*>? = null

    fun attachMediaPicker(picker : Picker<*>) {
        this.picker = picker
    }

    fun onImagePicked(uri : Uri?) {
        picker?.onImagePicked(uri)
    }

    fun onDismiss() {
        picker?.onDismiss()
    }
}