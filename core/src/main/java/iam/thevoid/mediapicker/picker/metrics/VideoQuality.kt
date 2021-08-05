package iam.thevoid.mediapicker.picker.metrics

/**
 * https://developer.android.com/reference/android/provider/MediaStore#EXTRA_VIDEO_QUALITY
 *
 * EXTRA_VIDEO_QUALITY
 * Added in API level 3
 *
 * public static final String EXTRA_VIDEO_QUALITY
 *
 * The name of the Intent-extra used to control the quality of a recorded video.
 * This is an integer property. Currently value 0 means low quality, suitable for MMS messages, and
 * alue 1 means high quality. In the future other quality levels may be added.
 *
 * Constant Value: "android.intent.extra.videoQuality"
 */
enum class VideoQuality(val code: Int) {
    HIGH(1),
    LOW(0)
}