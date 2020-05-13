package iam.thevoid.mediapicker.picker.metrics

import java.util.concurrent.TimeUnit

data class Duration @JvmOverloads constructor(
        private val duration: Long = -1,
        private val timeUnit: TimeUnit = TimeUnit.MILLISECONDS
) {

    val seconds: Long
        get() = timeUnit.toSeconds(duration)
}