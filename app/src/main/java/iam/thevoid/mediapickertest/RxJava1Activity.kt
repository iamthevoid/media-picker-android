package iam.thevoid.mediapickertest

import android.net.Uri
import iam.thevoid.mediapicker.picker.Purpose
import iam.thevoid.mediapicker.picker.options.ImageOptions
import iam.thevoid.mediapicker.picker.options.VideoOptions
import iam.thevoid.mediapicker.rx1.MediaPicker
import iam.thevoid.mediapicker.rx1.file
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.File

class RxJava1Activity : BaseActivity() {

    private var subscription: Subscription? = null

    override fun onPickImageSelect(options: ImageOptions) {
        subscription = MediaPicker.builder()
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
        subscription = MediaPicker.builder()
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
        subscription = MediaPicker.builder()
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
        subscription = MediaPicker.builder()
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
        subscription = MediaPicker.builder()
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

    private fun load(show: (Uri) -> Unit): Observable.Transformer<Uri, File> =
            Observable.Transformer { observable ->
                observable.observeOn(AndroidSchedulers.mainThread())
                        .doOnNext { show(it) }
                        .observeOn(Schedulers.io())
                        .compose(file(this))
            }

    private fun <T> loading() = Observable.Transformer<T, T> {
        it.doOnSubscribe { showProgress() }
            .doOnTerminate { hideProgress() }
    }

    override fun onDestroy() {
        subscription?.unsubscribe()
        super.onDestroy()
    }
}