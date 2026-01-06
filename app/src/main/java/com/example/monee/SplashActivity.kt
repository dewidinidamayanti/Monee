package com.example.monee

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

        val dot1 = findViewById<View>(R.id.dot1)
        val dot2 = findViewById<View>(R.id.dot2)
        val dot3 = findViewById<View>(R.id.dot3)

        val anim = AnimationUtils.loadAnimation(this, R.anim.dot_animation)

        // Start animations with delay
        dot1.startAnimation(anim)
        Handler(Looper.getMainLooper()).postDelayed({ dot2.startAnimation(anim) }, 200)
        Handler(Looper.getMainLooper()).postDelayed({ dot3.startAnimation(anim) }, 400)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 2000)
    }
}
