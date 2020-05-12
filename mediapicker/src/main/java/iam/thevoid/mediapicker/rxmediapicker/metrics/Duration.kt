package iam.thevoid.mediapicker.rxmediapicker.metrics

import java.util.concurrent.TimeUnit

data class Duration(private val duration: Long, private val timeUnit: TimeUnit) {

    val seconds: Long
        get() = timeUnit.toSeconds(duration)
}