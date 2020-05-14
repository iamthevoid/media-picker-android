package iam.thevoid.mediapickertest

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import iam.thevoid.ae.gone
import iam.thevoid.ae.hide
import iam.thevoid.ae.inflate
import iam.thevoid.ae.show
import iam.thevoid.e.format
import iam.thevoid.e.safe
import iam.thevoid.mediapicker.picker.metrics.MemorySize
import iam.thevoid.mediapicker.picker.metrics.Resolution
import iam.thevoid.mediapicker.picker.metrics.SizeUnit
import iam.thevoid.mediapicker.picker.options.ImageOptions
import iam.thevoid.mediapicker.rx1.MediaPicker
import iam.thevoid.mediapicker.rx1.file
import iam.thevoid.mediapicker.util.FileUtil
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.File

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var imageView: ImageView? = null
    private var videoView: VideoView? = null
    private var pathText: TextView? = null
    private var sizeText: TextView? = null
    private var resolutionText: TextView? = null
    private var pickImageButton: Button? = null
    private var pickVideoButton: Button? = null
    private var takeImageButton: Button? = null
    private var takeVideoButton: Button? = null
    private var fileInfo: View? = null

    private var lastImageOptions: ImageOptions? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imageView = findViewById(R.id.image)
        videoView = findViewById(R.id.video)
        pathText = findViewById(R.id.path)
        sizeText = findViewById(R.id.size)
        resolutionText = findViewById(R.id.resolution)
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
                imageOptionsDialog {
                    MediaPicker.builder()
                            .takePhoto(it)
                            .request(this)
                            .compose(load(::showImage))
                            .subscribe(::showFileInfo) { it.printStackTrace() }
                }

            R.id.pick_image ->
                imageOptionsDialog {
                    MediaPicker.builder()
                            .pickImage(it)
                            .request(this)
                            .compose(load(::showImage))
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(::showFileInfo) { it.printStackTrace() }
                }


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

    private fun imageOptionsDialog(onCustomized: (ImageOptions) -> Unit) {
        val view = inflate(R.layout.dialog_image_options)
        val widthEditText = view.findViewById<EditText>(R.id.width)
        val heightEditText = view.findViewById<EditText>(R.id.height)
        val sizeEditText = view.findViewById<EditText>(R.id.size)
        val preserveRatioCheckbox = view.findViewById<CheckBox>(R.id.preserveRatio)
        lastImageOptions?.apply {
            widthEditText.setText(maxResolution.width.takeIf { it > 0 }?.toString().safe())
            heightEditText.setText(maxResolution.height.takeIf { it > 0 }?.toString().safe())
            sizeEditText.setText(maxSize.bytes.takeIf { it > 0 }?.toString().safe())
            preserveRatioCheckbox.isChecked = preserveRatio
        }
        AlertDialog.Builder(this)
                .setView(view)
                .setMessage(getString(R.string.pick_image_description))
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    onCustomized(ImageOptions(
                            maxResolution = Resolution(
                                    width = widthEditText.number(),
                                    height = heightEditText.number()
                            ),
                            maxSize = MemorySize(
                                    size = sizeEditText.number(),
                                    unit = SizeUnit.KILOBYTE
                            ),
                            preserveRatio = preserveRatioCheckbox.isChecked
                    ).also { lastImageOptions = it })
                }
                .setNegativeButton(android.R.string.cancel) { _, _ -> }
                .show()
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
        imageView?.setImageURI(null)
        imageView?.setImageURI(uri)
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
        val isImage = file.extension.let { !FileUtil.isVideoExt(it) && !FileUtil.isGifExt(it) }
        resolutionText?.gone(!isImage)
        if (isImage) {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            resolutionText?.text = "${bitmap.width} X ${bitmap.height}"
        }
    }

    private fun EditText.number() =
            try {
                text.toString().takeIf { it.isNotBlank() }?.toInt() ?: -1
            } catch (e: Exception) {
                -1
            }
}