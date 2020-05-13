package iam.thevoid.mediapicker.picker.metrics

class MemorySize(private val size: Int = -1, private val unit: SizeUnit = SizeUnit.BYTE) {

    val bytes: Long
        get() = size * unit.bytes

}