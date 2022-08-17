package com.example.moviverse.view.activity

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.moviverse.R
import com.example.moviverse.databinding.ActivitySplashBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private lateinit var rocketAnimation: AnimationDrawable
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.progressIcon.apply {
            setBackgroundResource(R.drawable.animated_icon)
            rocketAnimation = background as AnimationDrawable
        }

        rocketAnimation.start()
    }

    override fun onStart() {
        super.onStart()
        val user = auth.currentUser
        if(user != null){
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(
                    Intent(this, MainActivity::class.java),
                    ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
                finish()
            }, 3000)
        }
        else{
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(
                    Intent(this, LoginActivity::class.java),
                    ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
            }, 3000)

        }
    }

}