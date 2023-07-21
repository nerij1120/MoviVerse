package com.example.movieapp.view.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import com.example.movieapp.base.BaseActivity
import com.example.movieapp.databinding.ActivityRegisterBinding
import com.example.movieapp.model.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import java.util.Timer
import java.util.TimerTask

class RegisterActivity : BaseActivity<ActivityRegisterBinding>() {
    override val bindingInflater: (LayoutInflater) -> ActivityRegisterBinding
        get() = { ActivityRegisterBinding.inflate(it) }
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
    }

    override fun setEvent() {
        binding.loginTxt.setOnClickListener {
            onBackPressed()
        }

        binding.registerBtn.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val name = binding.etName.text.toString().trim()

            //Validate User inputs
            val success = validateUserInput(email, password, name)

            if(success)
            {
                binding.progressBar.visibility = View.VISIBLE
                registerUser(email, password, name)
            }
        }

        //Text Changed Listener for Edit Text
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

        binding.etPassword.addTextChangedListener(object : TextWatcher {
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

        binding.etName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.etNameLayout.error = null
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
                                binding.etNameLayout.error = "Name is required"
                            }
                        }
                    }
                }, delay)
            }
        })
    }

    private fun registerUser(email: String, password: String, name: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    addUserToDatabase(email, name, task)
                } else {
                    binding.progressBar.visibility = View.GONE
                    displayError(task.exception?.message?:"Something went wrong!")
                }
            }
    }

    private fun addUserToDatabase(
        email: String,
        name: String,
        task: Task<AuthResult>
    ) {
        val mUser = User(email, name)
        auth.currentUser?.let { user ->
            FirebaseDatabase.getInstance().getReference("Users")
                .child(user.uid)
                .setValue(mUser).addOnCompleteListener {
                    if (it.isSuccessful) {
                        displayMessage("Create Account Successfully!")
                        auth.signOut()
                        onBackPressed()
                    } else {
                        auth.signOut()
                        displayError(task.exception?.message?:"Something went wrong!")
                        binding.progressBar.visibility = View.GONE
                    }
                }
        }
    }

    private fun validateUserInput(email: String, password: String, name: String): Boolean {
        binding.etEmailLayout.error = null
        binding.etPasswordLayout.error = null
        binding.etNameLayout.error = null
        return if(email == ""){
            binding.etEmailLayout.error = "Email is required"
            binding.etEmail.requestFocus()
            false
        }else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.etEmailLayout.error = "Invalid Email address"
            binding.etEmail.requestFocus()
            false
        }
        else if(name == ""){
            binding.etNameLayout.error = "Name is required"
            binding.etName.requestFocus()
            false
        }
        else if(password == ""){
            binding.etPasswordLayout.error = "Password is required"
            binding.etPassword.requestFocus()
            false
        }
        else if(password.length < 5){
            binding.etPasswordLayout.error = "Password need to be longer than 5 characters"
            binding.etPassword.requestFocus()
            false
        }else true
    }

}