package iam.thevoid.mediapicker.picker.permission

data class PermissionResult(
    val granted: List<String> = emptyList(),
    val notGranted: List<String> = emptyList(),
    val foreverDenied: List<String> = emptyList()
)