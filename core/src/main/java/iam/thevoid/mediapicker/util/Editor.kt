@file:JvmName("Editor")

package iam.thevoid.mediapicker.util

import iam.thevoid.e.orElse

/**
 * Created by iam on 22/08/2017.
 */

fun currentDateFilename(extension: String): String =
        "${filenamePrefix(extension).orElse("file")}_${formatDateToString("yyyy_MM_dd_HH_mm_ss")}$extension"

private fun filenamePrefix(extension: String): String =
        when {
            FileUtil.isVideoExt(extension) -> "video"
            FileUtil.isGifExt(extension) -> "anim"
            else -> "image"
        }
