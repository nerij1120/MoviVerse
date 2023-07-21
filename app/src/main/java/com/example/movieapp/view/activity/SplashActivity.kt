package com.example.movieapp.view.activity

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import com.example.movieapp.R
import com.example.movieapp.base.BaseActivity
import com.example.movieapp.databinding.ActivitySplashBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SplashActivity : BaseActivity<ActivitySplashBinding>() {
    override val bindingInflater: (LayoutInflater) -> ActivitySplashBinding
        get() = {
            ActivitySplashBinding.inflate(it)
        }
    private lateinit var rocketAnimation: AnimationDrawable
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        Handler(mainLooper).postDelayed({
            if (user != null){
                startActivity(
                    Intent(this, MainActivity::class.java),
                    ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
                finish()
            }else{
                startActivity(
                    Intent(this, LoginActivity::class.java),
                    ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
            }
        }, 3000)
    }

}