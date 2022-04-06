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
class MediaPicker : Picker<Flow<Uri?>>(), CoroutineScope by CoroutineScope(Dispatchers.IO) {

    private var channel: MutableSharedFlow<Uri?> = MutableSharedFlow()
    private var job: Job? = null

    override fun initStream(applyOptions: (Uri) -> Uri): Flow<Uri?> =
        channel
            .take(1)
            .onEach { job?.cancel() }
            .onCompletion { job?.cancel() }
            .map { if (it != null) applyOptions(it) else it }
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

        fun Permission.toAndroid() = let { it.value }

        context.asActivity().askForPermissions(*permissions.map(Permission::parse).toTypedArray()) {
            handler.onRequestPermissionsResult(
                PermissionResult(
                    granted = it.granted().map(Permission::toAndroid),
                    notGranted = (it.denied() - it.permanentlyDenied()).map(Permission::toAndroid),
                    foreverDenied = it.permanentlyDenied().map(Permission::toAndroid)
                )
            )
        }
    }

    override fun onResult(uri: Uri) {
        launch { channel.emit(uri) }
    }

    override fun onEmptyResult() {
        launch { channel.emit(null) }
    }

    class Builder1 : Builder<Flow<Uri?>, MediaPicker>() {
        override fun create(): MediaPicker =
            instanse ?: MediaPicker().also { instanse = it }
    }

    companion object {

        private var instanse: MediaPicker? = null

        fun builder(): Builder1 = Builder1()
    }
}