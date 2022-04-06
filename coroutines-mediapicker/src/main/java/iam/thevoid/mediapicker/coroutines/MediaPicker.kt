package iam.thevoid.mediapicker.coroutines

import android.content.Context
import android.net.Uri
import com.afollestad.assent.Permission
import com.afollestad.assent.askForPermissions
import iam.thevoid.ae.asActivity
import iam.thevoid.mediapicker.picker.Picker
import iam.thevoid.mediapicker.picker.permission.PermissionResult
import iam.thevoid.mediapicker.picker.permission.PermissionsHandler
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class MediaPicker : Picker<Flow<Uri>>(), CoroutineScope by CoroutineScope(Dispatchers.IO) {

    private var channel: MutableSharedFlow<Any> = MutableSharedFlow()

    override fun initStream(applyOptions: (Uri) -> Uri): Flow<Uri> =
        channel.take(1)
            .takeIfInstance<Uri>()
            .map { applyOptions(it) }
            .flowOn(Dispatchers.IO)

    override fun requestPermissions(
        context: Context,
        permissions: List<String>,
        handler: PermissionsHandler
    ) {
        if (permissions.isEmpty()) {
            handler.onRequestPermissionsResult(PermissionResult())
            return
        }

        context.asActivity().askForPermissions(*permissions.map(Permission::parse).toTypedArray()) {
            handler.onRequestPermissionsResult(
                PermissionResult(
                    granted = it.granted().map(Permission::value),
                    notGranted = (it.denied() - it.permanentlyDenied()).map(Permission::value),
                    foreverDenied = it.permanentlyDenied().map(Permission::value)
                )
            )
        }
    }

    override fun onResult(uri: Uri) {
        launch { channel.emit(uri) }
    }

    override fun onEmptyResult() {
        launch { channel.emit(Any()) }
    }

    class Builder1 : Builder<Flow<Uri>, MediaPicker>() {
        override fun create(): MediaPicker =
            instanse ?: MediaPicker().also { instanse = it }
    }

    companion object {

        private var instanse: MediaPicker? = null

        fun builder(): Builder1 = Builder1()
    }

    private inline fun <reified T> Flow<Any>.takeIfInstance(): Flow<T> {
        return takeWhile { it is T }.map { it as T }
    }
}