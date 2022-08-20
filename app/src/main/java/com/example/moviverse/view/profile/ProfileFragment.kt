package com.example.moviverse.view.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.moviverse.R
import com.example.moviverse.databinding.FragmentProfileBinding
import com.example.moviverse.model.User
import com.example.moviverse.view.activity.SplashActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase


class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var reference: DatabaseReference
    private lateinit var mListener: ValueEventListener
    private var isLoggedIn = false
    private var emailAuth = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(layoutInflater)

        auth = Firebase.auth

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.logoutBox.setOnClickListener {
            context?.let { it1 ->
                MaterialAlertDialogBuilder(it1, R.style.ThemeOverlay_App_MaterialAlertDialog)
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
            Toast.makeText(context, "Coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.favoritesBox.setOnClickListener {
            Toast.makeText(context, "Coming soon", Toast.LENGTH_SHORT).show()

        }

        binding.changePasswordBox.setOnClickListener {
            Toast.makeText(context, "Coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
                val providerId = profile.providerId
                Log.e("providerID", providerId)
                if (providerId == "google.com" || providerId == "facebook.com")
                    emailAuth = false
            }

            isLoggedIn = true
            val userID = user.uid
            mListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userProfile = snapshot.getValue(User::class.java)
                    if (userProfile != null && _binding != null) {
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
                    Snackbar.make(binding.root, error.message, Snackbar.LENGTH_SHORT).show()
                }
            }
            reference.child(userID).addValueEventListener(mListener)
        }
    }

}