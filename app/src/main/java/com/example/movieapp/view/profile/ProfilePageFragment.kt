package com.example.movieapp.view.profile

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.movieapp.R
import com.example.movieapp.base.BaseActivity
import com.example.movieapp.base.BaseFragment
import com.example.movieapp.databinding.FragmentProfilePageBinding
import com.example.movieapp.model.User
import com.example.movieapp.view.activity.LoginActivity
import com.example.movieapp.view.activity.MainActivity
import com.example.movieapp.view.activity.SplashActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase


class ProfilePageFragment : BaseFragment<FragmentProfilePageBinding>() {
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentProfilePageBinding
        get() = {
            layoutInflater, viewGroup, b ->
            FragmentProfilePageBinding.inflate(layoutInflater, viewGroup, b)
        }
    private lateinit var auth: FirebaseAuth
    private lateinit var reference: DatabaseReference
    private lateinit var mListener: ValueEventListener
    private var isLoggedIn = false
    private var emailAuth = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        auth = Firebase.auth
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun setEvent() {
        binding.logoutBox.setOnClickListener {
            context?.let { it1 ->
                MaterialAlertDialogBuilder(it1)
                    .setTitle("Want to log out?")
                    .setMessage("You sure you want to log out of your account?")
                    .setIcon(R.drawable.ic_baseline_logout_24)
                    .setPositiveButton("Log out") { _, _ ->
                        binding.progressBar.visibility = View.VISIBLE
                        auth.signOut()

                        startActivity(Intent(it1, SplashActivity::class.java))
                        activity?.finish()
                    }
                    .setNegativeButton("Cancel"){ _, _ ->}
                    .show()
            }
        }

        binding.accountBox.setOnClickListener {
            if(isLoggedIn)
                findNavController().navigate(
                    R.id.action_profilePageFragment_to_accountFragment
                )
            else{
                showSnackBar()
            }
        }

        binding.favoritesBox.setOnClickListener {
            if(isLoggedIn)
                findNavController().navigate(
                    R.id.action_profilePageFragment_to_favoritesFragment
                )
            else{
                showSnackBar()
            }
        }

        binding.changePasswordBox.setOnClickListener {
            if (!isLoggedIn){
                showSnackBar()
            }
            else if(!emailAuth){
                (activity as BaseActivity<*>).displayMessage(
                    "You have logged in with Facebook or Goggle, don't need to change password"
                )
            }
            else{
                findNavController().navigate(
                    R.id.action_profilePageFragment_to_changePasswordFragment
                )
            }
        }
    }

    private fun showSnackBar() {
        (activity as BaseActivity<*>).displayMessage("Please login to use this feature", "Login"){
            startActivity(
                Intent(activity, LoginActivity::class.java),
                ActivityOptions.makeSceneTransitionAnimation(activity).toBundle()
            )
        }
    }

    override fun onStop() {
        super.onStop()
        auth.currentUser?.uid?.let { reference.child(it).removeEventListener(mListener) }
    }

    override fun onStart() {
        super.onStart()
        addProfileListener()
    }

    private fun addProfileListener() {
        val user = auth.currentUser
        reference = FirebaseDatabase.getInstance().getReference("Users")
        if (user != null) {
            for (profile in user.providerData) {
                // Id of the provider (ex: google.com)
                if (profile.providerId == "password")
                    emailAuth = true
            }

            isLoggedIn = true
            val userID = user.uid
            mListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userProfile = snapshot.getValue(User::class.java)
                    if (userProfile != null && binding != null) {
                        binding.userDisplayName.text = userProfile.name
                        context?.let {
                            Glide.with(it)
                                .asBitmap()
                                .placeholder(R.drawable.ic_person)
                                .error(R.drawable.ic_avatar)
                                .load(userProfile.photo_url)
                                .into(binding.profilePic)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    (activity as MainActivity).displayError(error.message)
                }
            }
            reference.child(userID).addValueEventListener(mListener)
        }
    }

}