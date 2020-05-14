package iam.thevoid.mediapicker.picker.metrics

data class MemorySize(private val size: Int = -1, private val unit: SizeUnit = SizeUnit.BYTE) {

    val bytes: Long
        get() = size * unit.bytes

    val kiloBytes: Long
        get() = bytes / SizeUnit.KILOBYTE.bytes

}