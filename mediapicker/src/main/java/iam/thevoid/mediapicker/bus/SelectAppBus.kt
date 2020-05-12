package iam.thevoid.mediapicker.bus

import android.content.Context
import iam.thevoid.mediapicker.chooser.IntentData
import iam.thevoid.mediapicker.rxmediapicker.Picker

object SelectAppBus {
    private var picker: Picker<*>? = null

    fun attachMediaPicker(picker: Picker<*>) {
        SelectAppBus.picker = picker
    }

    fun onSelectApp(context: Context, intentData: IntentData?) {
        picker?.onAppSelect(context, intentData)
    }
}