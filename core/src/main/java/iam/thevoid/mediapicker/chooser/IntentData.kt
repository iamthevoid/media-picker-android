package iam.thevoid.mediapicker.chooser

import android.content.ComponentName
import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class IntentData(
        val intent: Intent,
        val requestCode: Int,
        val title: String? = null,
        val permissions: List<String> = emptyList()
) : Parcelable {

    fun setResolveInfo(resolveInfo: ResolveInfo) {
        val activity = resolveInfo.activityInfo
        val name = ComponentName(activity.applicationInfo.packageName,
                activity.name)
        intent.component = name
    }
}