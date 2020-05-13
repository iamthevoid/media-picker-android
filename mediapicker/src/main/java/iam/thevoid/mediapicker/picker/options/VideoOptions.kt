package iam.thevoid.mediapicker.picker.options

import iam.thevoid.mediapicker.R
import iam.thevoid.mediapicker.picker.metrics.MemorySize
import iam.thevoid.mediapicker.picker.metrics.VideoQuality
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds

@OptIn(ExperimentalTime::class)
data class VideoOptions @JvmOverloads constructor(
        val maxDuration: Duration = (-1).milliseconds,
        val maxSize: MemorySize = MemorySize(),
        val quality: VideoQuality = VideoQuality.HIGH,
        val chooserTitle: Int = R.string.take_video
)