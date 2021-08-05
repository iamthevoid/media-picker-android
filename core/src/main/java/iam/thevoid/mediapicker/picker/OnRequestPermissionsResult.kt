package iam.thevoid.mediapicker.picker

interface OnRequestPermissionsResult {
    fun onRequestPermissionsResult(granted: Boolean)
    fun onRequestPermissionsFailed(throwable: Throwable)
}