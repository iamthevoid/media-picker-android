package iam.thevoid.mediapicker.rx2

import android.content.Context
import android.net.Uri
import com.tbruyelle.rxpermissions2.RxPermissions
import iam.thevoid.ae.asFragmentActivity
import iam.thevoid.mediapicker.picker.OnRequestPermissionsResult
import iam.thevoid.mediapicker.picker.Picker
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.MaybeSubject

class MediaPicker : Picker<Maybe<Uri>>() {

    private var publishSubject: MaybeSubject<Uri>? = null
    private var permissionDisposable: Disposable? = null

    override fun initStream(applyOptions: (Uri) -> Uri): Maybe<Uri> =
            MaybeSubject.create<Uri>().also { publishSubject = it }
                    .doOnEvent { _, _ -> permissionDisposable?.dispose() }
                    .doOnComplete { permissionDisposable?.dispose() }
                    .observeOn(Schedulers.io())
                    .map { applyOptions(it) }

    override fun requestPermissions(context: Context, permissions: List<String>, result: OnRequestPermissionsResult) {
        permissionDisposable?.dispose()
        permissionDisposable = Observable.just(Any())
                .compose(RxPermissions(context.asFragmentActivity())
                        .ensure(*permissions.toTypedArray()))
                .subscribe(result::onRequestPermissionsResult, result::onRequestPermissionsFailed)
    }

    override fun onResult(uri: Uri) {
        publishSubject?.onSuccess(uri)
    }

    override fun onEmptyResult() {
        publishSubject?.onComplete()
    }

    class Builder1 : Builder<Maybe<Uri>, MediaPicker>() {
        override fun create(): MediaPicker =
                instanse ?: MediaPicker().also { instanse = it }
    }

    companion object {

        private var instanse: MediaPicker? = null

        fun  builder(): Builder1 = Builder1()
    }
}