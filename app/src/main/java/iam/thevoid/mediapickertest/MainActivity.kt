package iam.thevoid.mediapickertest

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import iam.thevoid.mediapicker.rx1.MediaPicker
import iam.thevoid.mediapicker.rx1.file
import iam.thevoid.mediapicker.rxmediapicker.Purpose
import java.io.File

class MainActivity : AppCompatActivity(), View.OnClickListener {

    var mImageView: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mImageView = findViewById(R.id.image)
        findViewById<Button>(R.id.btn_toast_filesize)?.setOnClickListener(this)
        findViewById<Button>(R.id.btn_just)?.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {

            R.id.btn_just -> MediaPicker.builder()
                    .pick(Purpose.Pick.IMAGE)
                    .take(Purpose.Take.PHOTO)
                    .build()
                    .request(this)
                    .subscribe(::loadImage)

            R.id.btn_toast_filesize -> MediaPicker.builder()
                    .pick(Purpose.Pick.IMAGE)
                    .take(Purpose.Take.PHOTO)
                    .build()
                    .request(this)
                    .compose(file(this))
                    .subscribe({ Toast.makeText(this, "File size is " + filesizeInMb(it), Toast.LENGTH_LONG).show() },
                            { Log.e(this::class.java.simpleName, "error", it) })
        }
    }

    private fun filesizeInMb(file: File): String {
        val size = file.length().toDouble()
        return "" + size / 1024 / 1024
    }

    private fun loadImage(filepath: Uri) {
        Glide
                .with(this)
                .load(filepath)
                .asBitmap()
                .into(mImageView)
    }
}