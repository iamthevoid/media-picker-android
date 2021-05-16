package iam.thevoid.mediapickertest

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class StartActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        findViewById<View>(R.id.button_rx_java_1).also {
            it.setOnClickListener { startActivity(Intent(this, RxJava1Activity::class.java)) }
        }
        findViewById<View>(R.id.button_rx_java_2).also {
            it.setOnClickListener { startActivity(Intent(this, RxJava2Activity::class.java)) }
        }
        findViewById<View>(R.id.button_rx_java_3).also {
            it.setOnClickListener { startActivity(Intent(this, RxJava3Activity::class.java)) }
        }
        findViewById<View>(R.id.button_coroutines).also {
            it.setOnClickListener { startActivity(Intent(this, CoroutinesActivity::class.java)) }
        }
    }

}