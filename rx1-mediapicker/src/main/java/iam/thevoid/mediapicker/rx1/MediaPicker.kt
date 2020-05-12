package iam.thevoid.mediapicker.rx1

import android.content.Context
import android.net.Uri
import com.tbruyelle.rxpermissions.RxPermissions
import iam.thevoid.ae.asActivity
import iam.thevoid.mediapicker.rxmediapicker.Picker
import rx.Observable
import rx.subjects.PublishSubject

class MediaPicker : Picker<Observable<Uri>>() {

    private var publishSubject: PublishSubject<Uri>? = null

    override fun request(context: Context): Observable<Uri> {
        Observable.just<Any?>(null)
                .compose(RxPermissions(context.asActivity())
                        .ensure(*needsPermissions()))
                .filter { it }
                .subscribe({ startSelection(context) }, { it.printStackTrace() })
        return PublishSubject.create<Uri>().also { publishSubject = it }
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
                instanse ?: MediaPicker().also { instanse = it }
    }

    companion object {

        private var instanse: MediaPicker? = null

        fun  builder(): Builder1 = Builder1()
    }
}