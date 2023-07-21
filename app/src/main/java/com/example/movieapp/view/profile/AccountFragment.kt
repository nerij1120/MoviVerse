package com.example.movieapp.view.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.movieapp.R
import com.example.movieapp.base.BaseFragment
import com.example.movieapp.databinding.FragmentAccountBinding
import com.example.movieapp.model.User
import com.example.movieapp.view.activity.MainActivity
import com.example.movieapp.view.dialogfragment.ReAuthenticateFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.Timer
import java.util.TimerTask

class AccountFragment : BaseFragment<FragmentAccountBinding>() {
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentAccountBinding
        get() = {
            layoutInflater, viewGroup, b ->
            FragmentAccountBinding.inflate(layoutInflater, viewGroup, b)
        }
    private lateinit var auth: FirebaseAuth
    private lateinit var reference: DatabaseReference
    private var uri: Uri? = null
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private lateinit var mListener: ValueEventListener
    private lateinit var newFragment: ReAuthenticateFragment
    private var isUploading = false
    private val imageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
            if(result.resultCode == Activity.RESULT_OK){
                val data: Intent? = result.data
                uri = data?.data!!
                binding.profilePic.setImageURI(uri)
                (activity as MainActivity).displayMessage("Set Image Successfully!")
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        auth = Firebase.auth
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun setEvent() {
        binding.cancelBtn.setOnClickListener {
            if(!isUploading)
                findNavController().navigateUp()
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
                        activity?.runOnUiThread {
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
                        activity?.runOnUiThread {
                            if (p0.toString().trim() == "") {
                                binding.etNameLayout.error = "Name is required"
                            }
                        }

                    }
                }, delay)
            }
        })

        binding.saveChangesBtn.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val name = binding.etName.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            if(validateUserInput(email, name)){
                binding.progressBar.visibility = View.VISIBLE
                if(uri != null && !isUploading)
                {
                    isUploading = true
                    updateProfile(email, name, phone)
                }
                else{
                    if(!isUploading){
                        isUploading = true
                        updateProfileWithOutImage(email, name, phone)
                    }
                }
            }
        }

        binding.changeAvatarBtn.setOnClickListener {
            if(!isUploading)
                chooseImage()
        }

        binding.profilePic.setOnClickListener {
            if(!isUploading)
                chooseImage()
        }
    }

    private fun updateProfileWithOutImage(mEmail: String, mName: String, mPhone: String) {
        val user = auth.currentUser
        user!!.updateEmail(mEmail)
            .addOnSuccessListener {
                Log.e("LOG", "Change Email Successfully")
                val profileUpdates = userProfileChangeRequest {
                    displayName = mName
                }
                user.updateProfile(profileUpdates)
                    .addOnSuccessListener {
                        Log.e("LOG", "Change Profile Successfully")
                        updateDatabase(user, mPhone)
                    }
                    .addOnFailureListener {
                        showErrorSnackBar(it)
                    }
            }
            .addOnFailureListener {
                try {
                    throw it
                }
                catch (e: FirebaseAuthRecentLoginRequiredException)
                {
                    showDialog()
                }
                catch (e: Exception){
                    showErrorSnackBar(it)
                }
            }
    }

    private fun updateDatabase(user: FirebaseUser, mPhone: String) {
        val mUser =
            user.email?.let { it1 ->
                user.displayName?.let { it2 ->
                    User(it1, it2, mPhone, user.photoUrl.toString())
                }
            }
        reference.child(user.uid)
            .setValue(mUser)
            .addOnSuccessListener {
                (activity as MainActivity).displayMessage("Save Changes Successfully!")
                findNavController().navigateUp()
                binding.progressBar.visibility = View.GONE
                isUploading = false
            }
            .addOnFailureListener {
                showErrorSnackBar(it)
            }
    }

    private fun showDialog(){
        binding.progressBar.visibility = View.GONE
        val fragmentManager = activity?.supportFragmentManager
        newFragment = ReAuthenticateFragment(onClick = {
            val email = binding.etEmail.text.toString().trim()
            val name = binding.etName.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            if(validateUserInput(email, name)){
                binding.progressBar.visibility = View.VISIBLE
                if(uri != null)
                {
                    if(!isUploading){
                        isUploading = true
                        updateProfile(email, name, phone)
                    }
                }
                else{
                    if(!isUploading){
                        isUploading = true
                        updateProfileWithOutImage(email, name, phone)
                    }
                }
                newFragment.dismiss()
            }
        })
        if (fragmentManager != null) {
            newFragment.show(fragmentManager, "fragment_filter")
        }
    }

    private fun chooseImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        imageLauncher.launch(intent)
    }


    private fun updateProfile(email: String, name: String, phone: String) {
        // Create a reference to user profile pictures
        val user = auth.currentUser
        val profilePic = storageReference.child("images/${user!!.uid}")
        uri?.let { uri1 ->
            profilePic.putFile(uri1)
                .addOnSuccessListener {
                    profilePic.downloadUrl
                        .addOnSuccessListener { url->
                            Log.e("LOG", "Upload Image Successfully")
                            user.updateEmail(email)
                                .addOnSuccessListener {
                                    Log.e("LOG", "Change Email Successfully")
                                    val profileUpdates = userProfileChangeRequest {
                                        displayName = name
                                        photoUri = url
                                    }
                                    user.updateProfile(profileUpdates)
                                        .addOnSuccessListener {
                                            updateDatabase(user, phone)
                                        }
                                        .addOnFailureListener {
                                            showErrorSnackBar(it)
                                        }
                                }
                                .addOnFailureListener {
                                    try {
                                        throw it
                                    }
                                    catch (e: FirebaseAuthRecentLoginRequiredException){
                                        showDialog()
                                    }
                                    catch (e: Exception){
                                        showErrorSnackBar(it)
                                    }
                                }
                        }
                        .addOnFailureListener {
                            showErrorSnackBar(it)
                        }
                }
                .addOnFailureListener {
                    showErrorSnackBar(it)
                }
        }
    }

    private fun showErrorSnackBar(it: Exception) {
        binding.progressBar.visibility = View.GONE
        it.localizedMessage?.let { it1 ->
            (activity as MainActivity).displayError(it1)
        }
        isUploading = false
    }

    private fun validateUserInput(email: String, name: String): Boolean {
        binding.etEmailLayout.error = null
        binding.etNameLayout.error = null
        binding.etPhoneLayout.error = null
        if(email.isEmpty()){
            binding.etEmailLayout.error = "Email is required"
            binding.etEmail.requestFocus()
            return false
        }else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Log.e("email", email)
            binding.etEmailLayout.error = "Invalid Email address"
            binding.etEmail.requestFocus()
            return false
        }
        else if(name.isEmpty()){
            binding.etNameLayout.error = "Name is required"
            binding.etName.requestFocus()
            return false
        }
        return true
    }

    override fun onStart() {
        super.onStart()
        addProfileListener()
    }

    private fun addProfileListener() {
        val user = auth.currentUser
        reference = FirebaseDatabase.getInstance().getReference("Users")
        if (user != null) {
            val userID = user.uid
            mListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userProfile = snapshot.getValue(User::class.java)
                    if (userProfile != null) {
                        binding.etEmail.setText(userProfile.email)
                        binding.etName.setText(userProfile.name)
                        binding.etPhone.setText(userProfile.phone_number)
                        if (uri == null)
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

    override fun onStop() {
        super.onStop()
        auth.currentUser?.uid?.let { reference.child(it).removeEventListener(mListener) }
    }

}