package iam.thevoid.mediapicker.rx1

import android.content.Context
import android.net.Uri
import com.tbruyelle.rxpermissions.RxPermissions
import iam.thevoid.ae.asActivity
import iam.thevoid.mediapicker.picker.OnRequestPermissionsResult
import iam.thevoid.mediapicker.picker.Picker
import rx.Observable
import rx.Subscription
import rx.subjects.PublishSubject

class MediaPicker : Picker<Observable<Uri>>() {

    private var publishSubject: PublishSubject<Uri>? = null
    private var subscription: Subscription? = null

    override fun initStream(): Observable<Uri> =
            PublishSubject.create<Uri>().also { publishSubject = it }
                    .doOnTerminate { subscription?.unsubscribe() }

    override fun requestPermissions(context: Context, permissions: List<String>, result: OnRequestPermissionsResult) {
        subscription?.unsubscribe()
        subscription = Observable.just<Any?>(null)
                .compose(RxPermissions(context.asActivity())
                        .ensure(*permissions.toTypedArray()))
                .subscribe(result::onRequestPermissionsResult, result::onRequestPermissionsFailed)
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

        fun  builder(): Builder1 = Builder1()
    }
}