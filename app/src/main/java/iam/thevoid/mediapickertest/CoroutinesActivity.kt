package iam.thevoid.mediapickertest

import android.net.Uri
import androidx.lifecycle.lifecycleScope
import iam.thevoid.mediapicker.coroutines.MediaPicker
import iam.thevoid.mediapicker.coroutines.file
import iam.thevoid.mediapicker.picker.Purpose
import iam.thevoid.mediapicker.picker.options.ImageOptions
import iam.thevoid.mediapicker.picker.options.VideoOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class CoroutinesActivity : BaseActivity() {

    private var job: Job? = null

    override fun onPickImageSelect(options: ImageOptions) {
        job = lifecycleScope.launch {
            MediaPicker.builder()
                    .setImageOptions(options)
                    .pick(Purpose.Pick.Image)
                    .onDismissAppSelect(this@CoroutinesActivity)
                    .onDismissPick(this@CoroutinesActivity)
                    .build()
                    .request(this@CoroutinesActivity)
                    .let(loading())
                    .let(load(::showImageOrVideo))
                    .collect { showFileInfo(it) }
        }
    }

    override fun onPickVideoSelect() {
        job = lifecycleScope.launch {
            MediaPicker.builder()
                    .pick(Purpose.Pick.Video)
                    .onDismissAppSelect(this@CoroutinesActivity)
                    .onDismissPick(this@CoroutinesActivity)
                    .build()
                    .request(this@CoroutinesActivity)
                    .let(loading())
                    .let(load(::showImageOrVideo))
                    .collect { showFileInfo(it) }
        }
    }

    override fun onTakePhotoSelect(options: ImageOptions) {
        job = lifecycleScope.launch {
            MediaPicker.builder()
                    .setImageOptions(options)
                    .take(Purpose.Take.Photo)
                    .onDismissAppSelect(this@CoroutinesActivity)
                    .onDismissPick(this@CoroutinesActivity)
                    .build()
                    .request(this@CoroutinesActivity)
                    .let(loading())
                    .let(load(::showImageOrVideo))
                    .collect { showFileInfo(it) }
        }
    }

    override fun onTakeVideoSelect(options: VideoOptions) {
        job = lifecycleScope.launch {
            MediaPicker.builder()
                    .setTakeVideoOptions(options)
                    .take(Purpose.Take.Video)
                    .onDismissAppSelect(this@CoroutinesActivity)
                    .onDismissPick(this@CoroutinesActivity)
                    .build()
                    .request(this@CoroutinesActivity)
                    .let(loading())
                    .let(load(::showImageOrVideo))
                    .collect { showFileInfo(it) }
        }
    }

    override fun onCustomPurpose(purpose: List<Purpose>) {
        job = lifecycleScope.launch {
            MediaPicker.builder()
                    .pick(*purpose.filterIsInstance<Purpose.Pick>().toTypedArray())
                    .take(*purpose.filterIsInstance<Purpose.Take>().toTypedArray())
                    .onDismissAppSelect(this@CoroutinesActivity)
                    .onDismissPick(this@CoroutinesActivity)
                    .build()
                    .request(this@CoroutinesActivity)
                    .let(loading())
                    .let(load(::showImageOrVideo))
                    .collect { showFileInfo(it) }
        }
    }

    private fun load(show: (Uri) -> Unit): Flow<Uri>.() -> Flow<File> = {
        onEach { show(it) }
                .flowOn(Dispatchers.Main)
                .let(file(this@CoroutinesActivity))
    }

    private fun <T> loading(): Flow<T>.() -> Flow<T> = {
        onStart { showProgress() }
            .onCompletion { hideProgress() }
            .onEach { hideProgress() }
    }

    override fun onDestroy() {
        job?.cancel()
        super.onDestroy()
    }
}