package iam.thevoid.mediapickertest

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_start.*

class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        button_rx_java_1.setOnClickListener { startActivity(Intent(this, RxJava1Activity::class.java)) }
        button_rx_java_2.setOnClickListener { startActivity(Intent(this, RxJava2Activity::class.java)) }
        button_rx_java_3.setOnClickListener { startActivity(Intent(this, RxJava3Activity::class.java)) }
        button_coroutines.setOnClickListener { startActivity(Intent(this, CoroutinesActivity::class.java)) }
    }

}