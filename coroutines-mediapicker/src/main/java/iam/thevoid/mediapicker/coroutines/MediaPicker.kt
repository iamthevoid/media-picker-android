package iam.thevoid.mediapicker.coroutines

import android.content.Context
import android.net.Uri
import com.afollestad.assent.Permission
import com.afollestad.assent.coroutines.awaitPermissionsGranted
import iam.thevoid.ae.asActivity
import iam.thevoid.mediapicker.picker.OnRequestPermissionsResult
import iam.thevoid.mediapicker.picker.Picker
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class MediaPicker : Picker<Flow<Uri>>(), CoroutineScope by CoroutineScope(Dispatchers.IO) {

    private var channel: ConflatedBroadcastChannel<Uri>? = null
    private var job: Job? = null

    override fun initStream(applyOptions: (Uri) -> Uri): Flow<Uri> =
            ConflatedBroadcastChannel<Uri>().also { channel = it }
                    .asFlow()
                    .onEach { job?.cancel() }
                    .onCompletion { job?.cancel() }
                    .map { applyOptions(it) }
                    .flowOn(Dispatchers.IO)

    override fun requestPermissions(context: Context, permissions: List<String>, result: OnRequestPermissionsResult) {
        job?.cancel()
        job = launch(Dispatchers.Main) {
            try {
                permissions
                        .map { Permission.parse(it) }
                        .toTypedArray()
                        .also {
                            context.asActivity().awaitPermissionsGranted(*it)
                        }
                result.onRequestPermissionsResult(granted = true)
            } catch (e: Exception) {
                result.onRequestPermissionsFailed(e)
            }
        }
    }

    override fun onResult(uri: Uri) {
        launch { channel?.send(uri) }
    }

    override fun onEmptyResult() {
        launch { channel?.close() }
    }

    class Builder1 : Builder<Flow<Uri>, MediaPicker>() {
        override fun create(): MediaPicker =
                instanse ?: MediaPicker().also { instanse = it }
    }

    companion object {

        private var instanse: MediaPicker? = null

        fun builder(): Builder1 = Builder1()
    }
}