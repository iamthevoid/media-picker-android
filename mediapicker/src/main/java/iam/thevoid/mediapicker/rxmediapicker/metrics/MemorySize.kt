package iam.thevoid.mediapicker.rxmediapicker.metrics

class MemorySize(private val size: Int, private val unit: SizeUnit) {

    val bytes: Long
        get() = size * unit.bytes

}