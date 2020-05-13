package iam.thevoid.mediapickertest

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import iam.thevoid.ae.gone
import iam.thevoid.ae.hide
import iam.thevoid.ae.show
import iam.thevoid.e.format
import iam.thevoid.mediapicker.rx1.MediaPicker
import iam.thevoid.mediapicker.rx1.file
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.File

class MainActivity : AppCompatActivity(), View.OnClickListener {

    var imageView: ImageView? = null
    var videoView: VideoView? = null
    var pathText: TextView? = null
    var sizeText: TextView? = null
    var pickImageButton: Button? = null
    var pickVideoButton: Button? = null
    var takeImageButton: Button? = null
    var takeVideoButton: Button? = null
    var fileInfo: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imageView = findViewById(R.id.image)
        videoView = findViewById(R.id.video)
        pathText = findViewById(R.id.path)
        sizeText = findViewById(R.id.size)
        pickImageButton = findViewById(R.id.pick_image)
        pickImageButton?.setOnClickListener(this)
        pickVideoButton = findViewById(R.id.pick_video)
        pickVideoButton?.setOnClickListener(this)
        takeImageButton = findViewById(R.id.take_photo)
        takeImageButton?.setOnClickListener(this)
        takeVideoButton = findViewById(R.id.take_video)
        takeVideoButton?.setOnClickListener(this)
        fileInfo = findViewById(R.id.file_info)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.take_photo ->
                MediaPicker.builder()
                        .takePhoto()
                        .request(this)
                        .compose(load(::showImage))
                        .subscribe(::showFileInfo) { it.printStackTrace() }

            R.id.pick_image ->
                MediaPicker.builder()
                        .pickImage()
                        .request(this)
                        .compose(load(::showImage))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(::showFileInfo) { it.printStackTrace() }


            R.id.take_video ->
                MediaPicker.builder()
                        .takeVideo()
                        .request(this)
                        .compose(load(::showVideo))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(::showFileInfo) { it.printStackTrace() }

            R.id.pick_video ->
                MediaPicker.builder()
                        .pickVideo()
                        .request(this)
                        .compose(load(::showVideo))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(::showFileInfo) { it.printStackTrace() }
        }
    }

    private fun filesizeInMb(file: File): String {
        val size = file.length().toDouble()
        return (size / 1024 / 1024).format(2)
    }


    private fun load(show: (Uri) -> Unit): Observable.Transformer<Uri, File> =
            Observable.Transformer { observable ->
                observable.observeOn(AndroidSchedulers.mainThread())
                        .doOnNext { show(it) }
                        .observeOn(Schedulers.io())
                        .compose(file(this))
            }

    private fun showImage(uri: Uri) {
        videoView?.gone()
        videoView?.stopPlayback()
        imageView?.show()
        Glide
                .with(this)
                .load(uri)
                .asBitmap()
                .into(imageView)
    }

    private fun showVideo(uri: Uri) {
        videoView?.show()
        imageView?.gone()
        videoView?.setVideoURI(uri)
        videoView?.start()
    }

    @SuppressLint("SetTextI18n")
    private fun showFileInfo(file: File) {
        val exists = file.exists()
        fileInfo.hide(!exists)
        if (!exists)
            return
        pathText?.text = "Path: ${file.absolutePath}"
        sizeText?.text = "Size: ${filesizeInMb(file)} MB"
    }
}