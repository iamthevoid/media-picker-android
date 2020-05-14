package iam.thevoid.mediapicker.builder

import android.content.Intent
import android.provider.MediaStore

class VideoIntentBuilder(
        private val videoDuration: Long? = null,
        private val videoQuality: Int? = null,
        private val videoFileSize: Long? = null
) {
    fun build(): Intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
        videoDuration?.takeIf { it > 0 }?.also { putExtra(MediaStore.EXTRA_DURATION_LIMIT, it) }
        videoQuality?.also { putExtra(MediaStore.EXTRA_VIDEO_QUALITY, it) }
        videoFileSize?.takeIf { it > 0 }?.also { putExtra(MediaStore.EXTRA_SIZE_LIMIT, videoFileSize) }
    }
}