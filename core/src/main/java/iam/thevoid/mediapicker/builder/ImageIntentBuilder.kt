package iam.thevoid.mediapicker.builder

import android.content.Intent

class ImageIntentBuilder {
    private var mimetype = MimeType.BOTH_IMAGE_AND_VIDEO
    private var localOnly = false

    fun setLocalOnly(localOnly: Boolean) = apply { this.localOnly = localOnly }

    fun setMimeType(mimeType: MimeType) = apply { this.mimetype = mimeType }

    fun build(): Intent = Intent(Intent.ACTION_PICK)
            .putExtra(Intent.EXTRA_LOCAL_ONLY, localOnly)
            .setType(mimetype.type)
            .setAction(Intent.ACTION_GET_CONTENT)

    enum class MimeType(val type: String) {
        IMAGE("image/*"),
        VIDEO("video/*"),
        BOTH_IMAGE_AND_VIDEO("image/*, video/*");
    }
}