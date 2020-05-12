package iam.thevoid.mediapicker.picker.metrics

class MemorySize(private val size: Int, private val unit: SizeUnit) {

    val bytes: Long
        get() = size * unit.bytes

}