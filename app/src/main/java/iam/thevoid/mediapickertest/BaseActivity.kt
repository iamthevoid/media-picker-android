package iam.thevoid.mediapickertest

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import iam.thevoid.mediapicker.picker.Picker
import iam.thevoid.mediapicker.picker.Purpose
import iam.thevoid.mediapicker.picker.metrics.MemorySize
import iam.thevoid.mediapicker.picker.metrics.Resolution
import iam.thevoid.mediapicker.picker.metrics.SizeUnit
import iam.thevoid.mediapicker.picker.metrics.VideoQuality
import iam.thevoid.mediapicker.picker.options.ImageOptions
import iam.thevoid.mediapicker.picker.options.VideoOptions
import iam.thevoid.mediapicker.picker.permission.PermissionResult
import iam.thevoid.mediapicker.picker.permission.PermissionsHandler
import iam.thevoid.mediapicker.util.FileUtil
import iam.thevoid.mediapicker.util.FileUtil.isImage
import iam.thevoid.mediapicker.util.FileUtil.isVideo
import java.io.File
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

abstract class BaseActivity : AppCompatActivity(), View.OnClickListener, Picker.OnDismissListener {

    private lateinit var pickImage: View
    private lateinit var pickVideo: View
    private lateinit var takePhoto: View
    private lateinit var takeVideo: View
    private lateinit var customize: View
    private lateinit var video: VideoView
    private lateinit var image: ImageView
    private lateinit var fileInfo: View
    private lateinit var path: TextView
    private lateinit var size: TextView
    private lateinit var resolution: TextView
    private lateinit var progress: View

    private var lastImageOptions: ImageOptions? = null
    private var lastVideoOptions: VideoOptions? = null
    private var lastPurposes: List<Purpose> = emptyList()

    protected val permissionsHandler = object : PermissionsHandler {
        override fun onRequestPermissionsResult(result: PermissionResult) {
            println("granted = ${result.granted.joinToString()}, denied = ${result.notGranted.joinToString()}, foreverDenied = ${result.foreverDenied.joinToString()}")
        }

        override fun onRequestPermissionsFailed(throwable: Throwable) {
            println(throwable.message)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo)
        pickImage = findViewById<View>(R.id.pick_image).also { it.setOnClickListener(this) }
        pickVideo = findViewById<View>(R.id.pick_video).also { it.setOnClickListener(this) }
        takePhoto = findViewById<View>(R.id.take_photo).also { it.setOnClickListener(this) }
        takeVideo = findViewById<View>(R.id.take_video).also { it.setOnClickListener(this) }
        customize = findViewById<View>(R.id.customize).also { it.setOnClickListener(this) }
        video = findViewById(R.id.video)
        image = findViewById(R.id.image)
        fileInfo = findViewById(R.id.file_info)
        path = findViewById(R.id.path)
        size = findViewById(R.id.size)
        resolution = findViewById(R.id.resolution)
        progress = findViewById(R.id.progress)

    }

    abstract fun onPickImageSelect(options: ImageOptions)
    abstract fun onPickVideoSelect()
    abstract fun onTakePhotoSelect(options: ImageOptions)
    abstract fun onTakeVideoSelect(options: VideoOptions)
    abstract fun onCustomPurpose(purpose: List<Purpose>)

    override fun onClick(v: View) {
        when (v.id) {
            R.id.take_photo -> imageOptionsDialog(::onTakePhotoSelect)
            R.id.pick_image -> imageOptionsDialog(::onPickImageSelect)
            R.id.take_video -> videoOptionsDialog(::onTakeVideoSelect)
            R.id.pick_video -> onPickVideoSelect()
            R.id.customize -> customOptionsDialog(::onCustomPurpose)
        }
    }

    private fun customOptionsDialog(onCustomized: (List<Purpose>) -> Unit) {
        val view = inflate(R.layout.dialog_custom)
        val pickImageCheckbox = view.findViewById<CheckBox>(R.id.pick_image_checkbox)
        val pickVideoCheckbox = view.findViewById<CheckBox>(R.id.pick_video_checkbox)
        val takePhotoCheckbox = view.findViewById<CheckBox>(R.id.take_photo_checkbox)
        val takeVideoCheckbox = view.findViewById<CheckBox>(R.id.take_video_checkbox)
        pickImageCheckbox.isChecked = lastPurposes.contains(Purpose.Pick.Image)
        pickVideoCheckbox.isChecked = lastPurposes.contains(Purpose.Pick.Video)
        takePhotoCheckbox.isChecked = lastPurposes.contains(Purpose.Take.Photo)
        takeVideoCheckbox.isChecked = lastPurposes.contains(Purpose.Take.Video)
        AlertDialog.Builder(this)
                .setView(view)
                .setMessage(getString(R.string.pick_image_description))
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    onCustomized(mutableListOf<Purpose>().apply {
                        if (pickImageCheckbox.isChecked)
                            add(Purpose.Pick.Image)
                        if (pickVideoCheckbox.isChecked)
                            add(Purpose.Pick.Video)
                        if (takePhotoCheckbox.isChecked)
                            add(Purpose.Take.Photo)
                        if (takeVideoCheckbox.isChecked)
                            add(Purpose.Take.Video)
                    }.also { lastPurposes = it })
                }
                .setNegativeButton(android.R.string.cancel) { _, _ -> }
                .show()
    }

    private fun imageOptionsDialog(onCustomized: (ImageOptions) -> Unit) {
        val view = inflate(R.layout.dialog_image_options)
        val widthEditText = view.findViewById<EditText>(R.id.width)
        val heightEditText = view.findViewById<EditText>(R.id.height)
        val sizeEditText = view.findViewById<EditText>(R.id.image_size)
        val preserveRatioCheckBox = view.findViewById<CheckBox>(R.id.preserve_ratio)
        lastImageOptions?.apply {
            widthEditText.setText(maxResolution.width.takeIf { it > 0 }?.toString().safe())
            heightEditText.setText(maxResolution.height.takeIf { it > 0 }?.toString().safe())
            sizeEditText.setText(maxSize.kiloBytes.takeIf { it > 0 }?.toString().safe())
            preserveRatioCheckBox.isChecked = preserveRatio
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
                            preserveRatio = preserveRatioCheckBox.isChecked
                    ).also { lastImageOptions = it })
                }
                .setNegativeButton(android.R.string.cancel) { _, _ -> }
                .show()
    }

    @OptIn(ExperimentalTime::class)
    private fun videoOptionsDialog(onCustomized: (VideoOptions) -> Unit) {
        val view = inflate(R.layout.dialog_video_options)
        val durationEditText = view.findViewById<EditText>(R.id.duration)
        val sizeEditText = view.findViewById<EditText>(R.id.video_size)
        val qualityRadioGroup = view.findViewById<RadioGroup>(R.id.quality_radio_group)
        lastVideoOptions?.apply {
            durationEditText.setText(
                maxDuration.inWholeMilliseconds.takeIf { it > 0 }?.toInt()?.toString().safe()
            )
            sizeEditText.setText(maxSize.kiloBytes.takeIf { it > 0 }?.toString().safe())
            qualityRadioGroup.check(
                when (quality) {
                    VideoQuality.LOW -> R.id.low
                    VideoQuality.HIGH -> R.id.high
                }
            )
        }
        AlertDialog.Builder(this)
                .setView(view)
                .setMessage(getString(R.string.pick_image_description))
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    onCustomized(VideoOptions(
                        maxDuration = Duration.milliseconds(durationEditText.number()),
                        maxSize = MemorySize(
                            size = sizeEditText.number(),
                            unit = SizeUnit.KILOBYTE
                        ),
                        quality = when (qualityRadioGroup.checkedRadioButtonId) {
                            R.id.low -> VideoQuality.LOW
                            R.id.high -> VideoQuality.HIGH
                            else -> VideoQuality.HIGH
                        }
                    ).also { lastVideoOptions = it })
                }
                .setNegativeButton(android.R.string.cancel) { _, _ -> }
                .show()
    }

    private fun fileSizeInMb(file: File): String {
        val size = file.length().toDouble()
        return (size / 1024 / 1024).format(2)
    }


    protected fun showImageOrVideo(uri: Uri) {
        when {
            uri.isImage(this) -> showImage(uri)
            uri.isVideo(this) -> showVideo(uri)
        }
    }


    private fun showImage(uri: Uri) {
        video.gone()
        video.stopPlayback()
        image.show()
        image.setImageURI(null)
        image.setImageURI(uri)
    }

    private fun showVideo(uri: Uri) {
        video.show()
        image.gone()
        video.setVideoURI(uri)
        video.start()
    }

    @SuppressLint("SetTextI18n")
    protected fun showFileInfo(file: File) {
        val exists = file.exists()
        fileInfo.hide(!exists)
        if (!exists)
            return


        path.text = "Path: ${file.absolutePath}"
        size.text = "Size: ${fileSizeInMb(file)} MB"
        val isImage = file.extension.let { !FileUtil.isVideoExt(it) }
        resolution.gone(!isImage)
        if (isImage) {
            BitmapFactory.decodeFile(file.absolutePath)?.also { bitmap ->
                resolution.text = "${bitmap.width} X ${bitmap.height}"
            }
        }
    }


    private fun EditText.number() =
        try {
            text.toString().takeIf { it.isNotBlank() }?.toInt() ?: -1
        } catch (e: Exception) {
            -1
        }

    override fun onDismiss() = progress.hide()

    fun showProgress() {
        Handler(Looper.getMainLooper()).post { progress.show() }
    }

    fun hideProgress() {
        Handler(Looper.getMainLooper()).post { progress.hide() }
    }
}