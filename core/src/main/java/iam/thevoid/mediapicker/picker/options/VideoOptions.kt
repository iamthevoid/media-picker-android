package iam.thevoid.mediapicker.picker.options

import iam.thevoid.mediapicker.picker.metrics.MemorySize
import iam.thevoid.mediapicker.picker.metrics.VideoQuality
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
data class VideoOptions constructor(
        val maxDuration: Duration = Duration.milliseconds(-1),
        val maxSize: MemorySize = MemorySize(),
        val quality: VideoQuality = VideoQuality.HIGH
) {
}