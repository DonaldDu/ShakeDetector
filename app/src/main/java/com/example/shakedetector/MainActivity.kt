package com.example.shakedetector

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.dhy.shakedetector.ShakeDetector

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ShakeDetector.onShake(this) {
            println("onShake")
        }
    }
}
