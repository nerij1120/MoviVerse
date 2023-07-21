package com.example.movieapp.view.profile

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.movieapp.base.BaseFragment
import com.example.movieapp.databinding.FragmentChangePasswordBinding
import com.example.movieapp.view.activity.MainActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.Timer
import java.util.TimerTask


class ChangePasswordFragment : BaseFragment<FragmentChangePasswordBinding>() {
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentChangePasswordBinding
        get() = {
            layoutInflater, viewGroup, b ->
            FragmentChangePasswordBinding.inflate(layoutInflater, viewGroup, b)
        }
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        auth = Firebase.auth
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun setEvent() {
        binding.etOldPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.etOldPasswordLayout.error = null
            }

            private val delay: Long = 1000
            private var timer = Timer()

            override fun afterTextChanged(p0: Editable?) {
                timer.cancel()
                timer = Timer()
                timer.schedule(object: TimerTask(){
                    override fun run() {
                        activity?.runOnUiThread {
                            if (p0.toString().trim() == "") {
                                binding.etOldPasswordLayout.error = "Old Password is required"
                            }
                            else if(p0.toString().trim().length < 6){
                                binding.etOldPasswordLayout.error = "Password need to be longer than 5 characters"
                            }
                        }
                    }
                }, delay)
            }
        })

        binding.etNewPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.etNewPasswordLayout.error = null
            }

            private val delay: Long = 1000
            private var timer = Timer()

            override fun afterTextChanged(p0: Editable?) {
                timer.cancel()
                timer = Timer()
                timer.schedule(object: TimerTask(){
                    override fun run() {
                        activity?.runOnUiThread {
                            if (p0.toString().trim() == "") {
                                binding.etNewPasswordLayout.error = "New Password is required"
                            }
                            else if(p0.toString().trim().length < 6){
                                binding.etNewPasswordLayout.error = "Password need to be longer than 5 characters"
                            }
                            else if(p0.toString().trim() != binding.etConfirmPassword.text.toString().trim()
                                && binding.etConfirmPassword.text.toString().trim() != ""){
                                binding.etConfirmPasswordLayout.error = "Confirm Password need to match New Password"
                            }
                        }
                    }
                }, delay)
            }
        })

        binding.etConfirmPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.etConfirmPasswordLayout.error = null
            }

            private val delay: Long = 1000
            private var timer = Timer()

            override fun afterTextChanged(p0: Editable?) {
                timer.cancel()
                timer = Timer()
                timer.schedule(object: TimerTask(){
                    override fun run() {
                        activity?.runOnUiThread {
                            when {
                                p0.toString().trim() == "" -> {
                                    binding.etConfirmPasswordLayout.error = "New Password is required"
                                }
                                p0.toString().trim().length < 6 -> {
                                    binding.etConfirmPasswordLayout.error = "Password need to be longer than 5 characters"
                                }
                                p0.toString().trim() != binding.etNewPassword.text.toString().trim() -> {
                                    binding.etConfirmPasswordLayout.error = "Confirm Password need to match New Password"
                                }
                            }
                        }
                    }
                }, delay)
            }
        })

        binding.cancelBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.changePasswordBtn.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            val oldPassword = binding.etOldPassword.text.toString().trim()
            val newPassword = binding.etNewPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()
            if(validateUserInput(oldPassword, newPassword, confirmPassword)){
                changePassword(oldPassword, newPassword)
            }
            else{
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun changePassword(oldPassword: String, newPassword: String) {
        val user = auth.currentUser
        val credential =
            user?.email?.let { it1 ->
                EmailAuthProvider.getCredential(it1, oldPassword)
            }
        if (credential != null) {
            user.reauthenticate(credential)
                .addOnSuccessListener {
                    user.updatePassword(newPassword)
                        .addOnSuccessListener {
                            binding.progressBar.visibility = View.GONE
                            (activity as MainActivity).displayMessage("Change Password Successfully!")
                            findNavController().navigateUp()
                        }
                        .addOnFailureListener { e ->
                            e.localizedMessage?.let {
                                (activity as MainActivity).displayMessage(it)
                            }
                            binding.progressBar.visibility = View.GONE
                        }
                }
                .addOnFailureListener { e ->
                    e.localizedMessage?.let {
                        (activity as MainActivity).displayMessage(it)
                    }
                    binding.progressBar.visibility = View.GONE
                }
        }
    }

    private fun validateUserInput(oldPassword: String, newPassword: String, confirmPassword: String): Boolean {
        binding.etOldPasswordLayout.error = null
        binding.etNewPasswordLayout.error = null
        binding.etConfirmPasswordLayout.error = null
        if(oldPassword == ""){
            binding.etOldPasswordLayout.error = "Old Password is required"
            binding.etOldPasswordLayout.requestFocus()
            return false
        }
        else if(oldPassword.length < 5){
            binding.etOldPasswordLayout.error = "Password need to be longer than 5 characters"
            binding.etOldPasswordLayout.requestFocus()
            return false
        }
        if(newPassword == ""){
            binding.etNewPasswordLayout.error = "New Password is required"
            binding.etNewPasswordLayout.requestFocus()
            return false
        }
        else if(newPassword.length < 5){
            binding.etNewPasswordLayout.error = "Password need to be longer than 5 characters"
            binding.etNewPasswordLayout.requestFocus()
            return false
        }
        if(confirmPassword == ""){
            binding.etConfirmPasswordLayout.error = "Confirm Password is required"
            binding.etConfirmPasswordLayout.requestFocus()
            return false
        }
        else if(confirmPassword.length < 5){
            binding.etConfirmPasswordLayout.error = "Password need to be longer than 5 characters"
            binding.etConfirmPasswordLayout.requestFocus()
            return false
        }
        if(newPassword != confirmPassword){
            binding.etConfirmPasswordLayout.error = "Confirm Password need to match New Password"
            binding.etConfirmPasswordLayout.requestFocus()
            return false
        }
        return true
    }
}