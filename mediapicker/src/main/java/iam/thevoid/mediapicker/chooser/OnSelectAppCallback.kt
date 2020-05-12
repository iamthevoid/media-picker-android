package iam.thevoid.mediapicker.chooser

import android.content.Context

interface OnSelectAppCallback {
    fun onSelectApp(context: Context, intentData: IntentData?)
}