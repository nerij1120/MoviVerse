package com.example.movieapp.view.dialogfragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.movieapp.databinding.FragmentReAuthenticateBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.Timer
import java.util.TimerTask


class ReAuthenticateFragment(val onClick: ()->Unit) : DialogFragment() {
    private var _binding: FragmentReAuthenticateBinding? = null
    private val binding get() = _binding as FragmentReAuthenticateBinding
    private lateinit var auth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentReAuthenticateBinding.inflate(layoutInflater)

        auth = Firebase.auth

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cancelBtn.setOnClickListener {
            dismiss()
        }

        binding.confirmBtn.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            val password = binding.etPassword.text.toString().trim()
            if(validatePassword(password)){
                val user = auth.currentUser
                val credential =
                    user?.email?.let { it1 -> EmailAuthProvider.getCredential(it1, password) }
                if (credential != null) {
                    user.reauthenticate(credential)
                        .addOnSuccessListener {
                            onClick.invoke()
                        }
                        .addOnFailureListener {
                            binding.progressBar.visibility = View.GONE
                            binding.etPasswordLayout.error = it.localizedMessage
                            binding.etPassword.requestFocus()
                        }
                }
            }
            else{
                binding.progressBar.visibility = View.GONE
            }
        }

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
                            if(_binding != null){
                                if (p0.toString() == "") {
                                    binding.etPasswordLayout.error = "Password is required"
                                }
                                else if(p0.toString().length < 6){
                                    binding.etPasswordLayout.error = "Password need to be longer than 5 characters"
                                }
                            }
                        }
                    }
                }, delay)
            }
        })
    }

    private fun validatePassword(password: String): Boolean {
        if (password == "") {
            binding.etPasswordLayout.error = "Password is required"
            binding.etPassword.requestFocus()
            return false
        }
        else if(password.length < 6){
            binding.etPasswordLayout.error = "Password need to be longer than 5 characters"
            binding.etPassword.requestFocus()
            return false
        }
        return true
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}