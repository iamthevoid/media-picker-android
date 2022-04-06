package iam.thevoid.mediapicker.rx1

import android.content.Context
import android.net.Uri
import com.tbruyelle.rxpermissions.RxPermissions
import iam.thevoid.ae.asActivity
import iam.thevoid.mediapicker.picker.Picker
import iam.thevoid.mediapicker.picker.permission.PermissionResult
import iam.thevoid.mediapicker.picker.permission.PermissionsHandler
import rx.Observable
import rx.Subscription
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject

class MediaPicker : Picker<Observable<Uri>>() {

    private var publishSubject: PublishSubject<Uri>? = null
    private var subscription: Subscription? = null

    override fun initStream(applyOptions: (Uri) -> Uri): Observable<Uri> =
        PublishSubject.create<Uri>().also { publishSubject = it }
            .doOnTerminate { subscription?.unsubscribe() }
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
        subscription?.unsubscribe()
        subscription = RxPermissions(context.asActivity())
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
        publishSubject?.onNext(uri)
        publishSubject?.onCompleted()
    }

    override fun onEmptyResult() {
        publishSubject?.onCompleted()
    }

    class Builder1 : Builder<Observable<Uri>, MediaPicker>() {
        override fun create(): MediaPicker =
            instance ?: MediaPicker().also { instance = it }
    }

    companion object {

        private var instance: MediaPicker? = null

        fun builder(): Builder1 = Builder1()
    }
}