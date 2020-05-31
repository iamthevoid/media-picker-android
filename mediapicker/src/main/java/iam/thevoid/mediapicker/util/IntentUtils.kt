package iam.thevoid.mediapicker.util

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo

/**
 * Created by iam on 22/08/2017.
 */
object IntentUtils {
    fun getResolveInfoList(pm: PackageManager, intents: List<Intent>) =
            intents.map { pm.queryIntentActivities(it, 0) }.flatten()

    fun getResolveInfoList(pm: PackageManager, intent: Intent): List<ResolveInfo> =
            getResolveInfoList(pm, listOf(intent))
}