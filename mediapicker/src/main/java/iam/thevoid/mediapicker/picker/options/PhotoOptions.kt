package iam.thevoid.mediapicker.picker.options

import iam.thevoid.mediapicker.R
import iam.thevoid.mediapicker.picker.metrics.MemorySize
import iam.thevoid.mediapicker.picker.metrics.Resolution

data class PhotoOptions @JvmOverloads constructor(
        val maxResolution: Resolution = Resolution(),
        val maxSize: MemorySize = MemorySize(),
        val chooserTitle: Int = R.string.take_photo
)