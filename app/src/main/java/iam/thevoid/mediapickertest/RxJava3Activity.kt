package iam.thevoid.mediapickertest

import android.net.Uri
import android.os.Handler
import android.os.Looper
import iam.thevoid.mediapicker.picker.Purpose
import iam.thevoid.mediapicker.picker.options.ImageOptions
import iam.thevoid.mediapicker.picker.options.VideoOptions
import iam.thevoid.mediapicker.rx3.MediaPicker
import iam.thevoid.mediapicker.rx3.file
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.MaybeTransformer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File

class RxJava3Activity : BaseActivity() {

    private var disposable: Disposable? = null

    override fun onPickImageSelect(options: ImageOptions) {
        disposable = MediaPicker.builder()
                .setImageOptions(options)
                .pick(Purpose.Pick.Image)
                .onDismissAppSelect(this)
                .onDismissPick(this)
                .build()
                .request(this)
                .compose(loading())
                .compose(load(::showImageOrVideo))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(::showFileInfo) { it.printStackTrace() }
    }

    override fun onPickVideoSelect() {
        disposable = MediaPicker.builder()
                .pick(Purpose.Pick.Video)
                .onDismissAppSelect(this)
                .onDismissPick(this)
                .build()
                .request(this)
                .compose(loading())
                .compose(load(::showImageOrVideo))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(::showFileInfo) { it.printStackTrace() }
    }

    override fun onTakePhotoSelect(options: ImageOptions) {
        disposable = MediaPicker.builder()
                .setImageOptions(options)
                .take(Purpose.Take.Photo)
                .onDismissAppSelect(this)
                .onDismissPick(this)
                .build()
                .request(this)
                .compose(loading())
                .compose(load(::showImageOrVideo))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(::showFileInfo) { it.printStackTrace() }
    }

    override fun onTakeVideoSelect(options: VideoOptions) {
        disposable = MediaPicker.builder()
                .setTakeVideoOptions(options)
                .take(Purpose.Take.Video)
                .onDismissAppSelect(this)
                .onDismissPick(this)
                .build()
                .request(this)
                .compose(loading())
                .compose(load(::showImageOrVideo))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(::showFileInfo) { it.printStackTrace() }
    }

    override fun onCustomPurpose(purpose: List<Purpose>) {
        disposable = MediaPicker.builder()
                .pick(*purpose.filterIsInstance<Purpose.Pick>().toTypedArray())
                .take(*purpose.filterIsInstance<Purpose.Take>().toTypedArray())
                .onDismissAppSelect(this)
                .onDismissPick(this)
                .build()
                .request(this)
                .compose(loading())
                .compose(load(::showImageOrVideo))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(::showFileInfo) { it.printStackTrace() }
    }

    private fun load(show: (Uri) -> Unit): MaybeTransformer<Uri, File> =
            MaybeTransformer { observable ->
                observable.observeOn(AndroidSchedulers.mainThread())
                        .doOnSuccess { show(it) }
                        .observeOn(Schedulers.io())
                        .compose(file(this))
            }

    private fun <T> loading() = MaybeTransformer<T, T> {
        it.doOnSubscribe { Handler(Looper.getMainLooper()).post { showProgress() } }
            .doOnTerminate { Handler(Looper.getMainLooper()).post { hideProgress() } }
    }

    override fun onDestroy() {
        disposable?.dispose()
        super.onDestroy()
    }
}