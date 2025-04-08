package com.software.mymusicplayer

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    @SuppressLint("ObjectAnimatorBinding", "Recycle")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        ObjectAnimator.ofFloat(this,"alpha",1f,0f).apply {
            duration = 1000
            start()
        }.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationResume(animation: Animator) {
                super.onAnimationResume(animation)
                finish()
                startActivity(Intent(this@SplashActivity,LoginActivity::class.java))
            }
        })
    }
}