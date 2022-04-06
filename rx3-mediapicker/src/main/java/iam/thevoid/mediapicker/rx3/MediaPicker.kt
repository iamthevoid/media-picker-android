package iam.thevoid.mediapicker.rx3

import android.content.Context
import android.net.Uri
import com.tbruyelle.rxpermissions3.RxPermissions
import iam.thevoid.ae.asFragmentActivity
import iam.thevoid.mediapicker.picker.Picker
import iam.thevoid.mediapicker.picker.permission.PermissionResult
import iam.thevoid.mediapicker.picker.permission.PermissionsHandler
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.MaybeSubject

class MediaPicker : Picker<Maybe<Uri>>() {

    private var publishSubject: MaybeSubject<Uri>? = null
    private var permissionDisposable: Disposable? = null

    override fun initStream(applyOptions: (Uri) -> Uri): Maybe<Uri> =
        MaybeSubject.create<Uri>().also { publishSubject = it }
            .doOnEvent { _, _ -> permissionDisposable?.dispose() }
            .doOnComplete { permissionDisposable?.dispose() }
            .observeOn(Schedulers.io())
            .map { applyOptions(it) }

    override fun requestPermissions(
        context: Context,
        permissions: List<String>,
        handler: PermissionsHandler
    ) {
        if (permissions.isEmpty()) {
            handler.onRequestPermissionsResult(PermissionResult())
            return
        }
        permissionDisposable = RxPermissions(context.asFragmentActivity())
            .requestEach(*permissions.toTypedArray())
            .toList()
            .map { result ->
                val granted = result.filter { it.granted }
                val notGranted = result - granted
                val canBeRequested = notGranted.filter { it.shouldShowRequestPermissionRationale }
                val canNotBeRequested = notGranted - canBeRequested
                PermissionResult(
                    granted = granted.map { it.name },
                    notGranted = canBeRequested.map { it.name },
                    foreverDenied = canNotBeRequested.map { it.name }
                )
            }
            .subscribe(handler::onRequestPermissionsResult, handler::onRequestPermissionsFailed)
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

        fun builder(): Builder1 = Builder1()
    }
}