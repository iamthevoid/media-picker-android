package iam.thevoid.mediapicker.picker.options

import iam.thevoid.mediapicker.picker.metrics.MemorySize
import iam.thevoid.mediapicker.picker.metrics.Resolution

/**
 * @param maxResolution Max resolution in pixels for photo. Can be anything. Taken photo will be
 * scaled for fit both of maximums. You can preserve ratio or not with [preserveRatio]
 * @param preserveRatio Uses only if resolution set. Serves to preserve ratio when [maxResolution]
 * ratio differs from default photo ratio. If true (default) you got as result photo with default
 * ratio limited by [maxResolution] width or height, if false photo scale to reach [maxResolution]
 * width and height.
 * @param maxSize limits photo file size
 */
data class ImageOptions @JvmOverloads constructor(
        val maxResolution: Resolution = Resolution(),
        val preserveRatio: Boolean = true,
        val maxSize: MemorySize = MemorySize()
)