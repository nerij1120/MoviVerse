package com.example.moviverse.view.activity

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Pair
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.moviverse.R
import com.example.moviverse.databinding.ActivityLoginBinding
import com.example.moviverse.model.User
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import java.util.*


class LoginActivity : AppCompatActivity() {
    companion object{
        const val TAG = "LOGIN ACTIVITY"
    }
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private var loginSuccess = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.loginBtn.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val success = validateUserInput(email, password)

            //Validate User Inputs
            if(success){
                loginWithEmail(email, password)
            }
            else{
                binding.progressBar.visibility = View.GONE
            }
        }

        binding.signUpTxt.setOnClickListener {
            startActivity(
                Intent(this, RegisterActivity::class.java),
                ActivityOptions.makeSceneTransitionAnimation(this,
                    Pair.create(binding.etEmailLayout, "email"),
                    Pair.create(binding.etPasswordLayout, "password")
                ).toBundle())
        }

        binding.googleIc.setOnClickListener {
            Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.facebookIc.setOnClickListener {
            Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.guestLoginBtn.setOnClickListener {
            finishAffinity()
            startActivity(Intent(this, MainActivity::class.java),
                ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
            )
        }

        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.etEmailLayout.error = null
            }

            private val delay: Long = 1000
            private var timer = Timer()

            override fun afterTextChanged(p0: Editable?) {
                timer.cancel()
                timer = Timer()
                timer.schedule(object: TimerTask(){
                    override fun run() {
                        Handler(Looper.getMainLooper()).post {
                            if (p0.toString().trim() == "") {
                                binding.etEmailLayout.error = "Email is required"
                            }
                            else if(!Patterns.EMAIL_ADDRESS.matcher(p0.toString().trim()).matches()){
                                binding.etEmailLayout.error = "Invalid email address"
                            }
                        }
                    }
                }, delay)
            }
        })

        binding.etPassword.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.etPasswordLayout.error = null
            }

            private val delay: Long = 1000
            private var timer = Timer()

            override fun afterTextChanged(p0: Editable?) {
                timer.cancel()
                timer = Timer()
                timer.schedule(object: TimerTask(){
                    override fun run() {
                        Handler(Looper.getMainLooper()).post {
                            if (p0.toString().trim() == "") {
                                binding.etPasswordLayout.error = "Password is required"
                            }
                            else if(p0.toString().trim().length < 6){
                                binding.etPasswordLayout.error = "Password need to be longer than 5 characters"
                            }
                        }
                    }
                }, delay)
            }
        })

    }

    private fun loginWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    //Redirect
                    Toast.makeText(this, "Welcome back", Toast.LENGTH_SHORT).show()
                    loginSuccess = true
                    onBackPressed()
                } else {
                    Snackbar.make(binding.loginBtn, "${task.exception?.message}", Snackbar.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.GONE
                }
            }
    }

    override fun onStart() {
        super.onStart()
        val user = auth.currentUser
        if(user != null){
            loginSuccess = true
            onBackPressed()
        }
    }

    private fun validateUserInput(email: String, password: String): Boolean {
        binding.etEmailLayout.error = null
        binding.etPasswordLayout.error = null
        if(email == ""){
            binding.etEmailLayout.error = "Email is required"
            binding.etEmailLayout.requestFocus()
            return false
        }else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.etEmailLayout.error = "Invalid Email address"
            binding.etEmailLayout.requestFocus()
            return false
        }
        else if(password == ""){
            binding.etPasswordLayout.error = "Password is required"
            binding.etPasswordLayout.requestFocus()
            return false
        }
        else if(password.length < 5){
            binding.etPasswordLayout.error = "Password need to be longer than 5 characters"
            binding.etPasswordLayout.requestFocus()
            return false
        }
        return true
    }

    override fun onBackPressed() {
        if(loginSuccess)
            super.onBackPressed()
        else
            finishAffinity()
    }
}
