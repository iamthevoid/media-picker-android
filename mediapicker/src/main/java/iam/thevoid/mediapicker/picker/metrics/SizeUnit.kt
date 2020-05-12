package iam.thevoid.mediapicker.picker.metrics

enum class SizeUnit(var bytes: Long) {
    BYTE(1),
    KILOBYTE(1024 * BYTE.bytes),
    MEGABYTE(1024 * KILOBYTE.bytes),
    GIGABYTE(1024 * MEGABYTE.bytes);

    val bits: Long
        get() = 8 * bytes
}