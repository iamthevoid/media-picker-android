package iam.thevoid.mediapicker.picker.permission

interface PermissionsHandler {
    fun onRequestPermissionsResult(result: PermissionResult) = Unit
    fun onRequestPermissionsFailed(throwable: Throwable) = Unit
}