package iam.thevoid.mediapickertest

import android.net.Uri
import iam.thevoid.mediapicker.picker.Purpose
import iam.thevoid.mediapicker.picker.options.ImageOptions
import iam.thevoid.mediapicker.picker.options.VideoOptions
import iam.thevoid.mediapicker.rx2.MediaPicker
import iam.thevoid.mediapicker.rx2.copyFileToAppDir
import io.reactivex.MaybeTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.File

class RxJava2Activity : BaseActivity() {

    private var disposable: Disposable? = null

    override fun onPickImageSelect(options: ImageOptions) {
        disposable = MediaPicker.builder()
            .setPermissionResultHandler(permissionsHandler)
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
            .setPermissionResultHandler(permissionsHandler)
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
            .setPermissionResultHandler(permissionsHandler)
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
            .setPermissionResultHandler(permissionsHandler)
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
            .setPermissionResultHandler(permissionsHandler)
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
                .compose(copyFileToAppDir(this))
        }

    private fun <T> loading() = MaybeTransformer<T, T> {
        it.doOnSubscribe { showProgress() }
            .doOnComplete { hideProgress() }
            .doOnSuccess { hideProgress() }
            .doOnError { hideProgress() }
    }

    override fun onDestroy() {
        disposable?.dispose()
        super.onDestroy()
    }
}