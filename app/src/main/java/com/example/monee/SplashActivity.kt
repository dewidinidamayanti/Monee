package com.example.monee

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.example.monee.databinding.SplashScreenBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: SplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val anim = AnimationUtils.loadAnimation(this, R.anim.dot_animation)

        // Start animations with delay using View Binding
        binding.dot1.startAnimation(anim)
        Handler(Looper.getMainLooper()).postDelayed({ binding.dot2.startAnimation(anim) }, 200)
        Handler(Looper.getMainLooper()).postDelayed({ binding.dot3.startAnimation(anim) }, 400)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 2000)
    }
}
