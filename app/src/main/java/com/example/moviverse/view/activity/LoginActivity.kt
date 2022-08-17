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
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private lateinit var signUpRequest: BeginSignInRequest
    private lateinit var callbackManager: CallbackManager
    private var showOneTap = true
    private var loginSuccess = false
    private val signUpLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()){ result->
            try {
                binding.progressBar.visibility = View.VISIBLE
                val data = result.data
                val credential = oneTapClient.getSignInCredentialFromIntent(data)
                val idToken = credential.googleIdToken
                when {
                    idToken != null -> {
                        // Got an ID token from Google. Use it to authenticate
                        // with your backend.
                        Log.d(TAG, "Got ID token.")
                        // Create firebase account using google Token
                        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                        Log.e("token", idToken)
                        auth.signInWithCredential(firebaseCredential)
                            .addOnCompleteListener(this) { task ->
                                if (task.isSuccessful) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "signInWithCredential:success")
                                    // Send user to home screen
                                    val ggUser = auth.currentUser
                                    verifyWithFirebase(ggUser, task)
                                }
                                else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                                    task.exception?.message?.let {
                                        Snackbar.make(binding.googleIc,
                                            it, Snackbar.LENGTH_SHORT).show()
                                    }
                                    binding.progressBar.visibility = View.GONE
                                }
                            }
                    }
                    else -> {
                        // Shouldn't happen.
                        Log.d(TAG, "No ID token!")
                        binding.progressBar.visibility = View.GONE
                        Snackbar.make(binding.googleIc, "Something wrong happened!", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }catch (e: ApiException){
                when (e.statusCode) {
                    CommonStatusCodes.CANCELED -> {
                        Log.d(TAG, "One-tap dialog was closed.")
                        // Don't re-prompt the user.
                        showOneTap = false
                        binding.progressBar.visibility = View.GONE
                    }
                    CommonStatusCodes.NETWORK_ERROR -> {
                        Log.d(TAG, "One-tap encountered a network error.")
                        // Try again or just ignore.
                        Snackbar.make(binding.googleIc, "One-tap encountered a network error.", Snackbar.LENGTH_SHORT).show()
                        binding.progressBar.visibility = View.GONE
                    }
                    else -> {
                        Log.d(
                            TAG, "Couldn't get credential from result." +
                                " (${e.localizedMessage})")
                        Snackbar.make(binding.googleIc, "Couldn't get credential from result." +
                                " (${e.localizedMessage})", Snackbar.LENGTH_SHORT).show()
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }
        }
        private fun verifyWithFirebase(
            ggUser: FirebaseUser?,
            task: Task<AuthResult>
        ) {

            val mUser = ggUser?.displayName?.let {
                ggUser.email?.let { it1 ->
                    User(it1, it, ggUser.phoneNumber, ggUser.photoUrl.toString())
                }
            }

            if (task.result.additionalUserInfo?.isNewUser == true) {
                auth.currentUser?.let { user ->
                    FirebaseDatabase.getInstance().getReference("Users")
                        .child(user.uid)
                        .setValue(mUser).addOnCompleteListener {
                            if (it.isSuccessful) {
                                Log.e("Create account", "successfully")
                                Toast.makeText(
                                    this,
                                    "Create account successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                                loginSuccess = true
                                onBackPressed()
                            } else {
                                Log.e("Create account", "failed")
                                binding.progressBar.visibility = View.GONE
                                Snackbar.make(
                                    binding.googleIc,
                                    "${task.exception?.message}",
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
            } else {
                Toast.makeText(
                    this,
                    "Welcome back",
                    Toast.LENGTH_SHORT
                ).show()
                loginSuccess = true
                onBackPressed()
            }
        }

        private val signInLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()){ result->
                try {
                    binding.progressBar.visibility = View.VISIBLE
                    val data = result.data
                    val credential = oneTapClient.getSignInCredentialFromIntent(data)
                    val idToken = credential.googleIdToken
                    val username = credential.id
                    val password = credential.password
                    when {
                        idToken != null -> {
                            // Got an ID token from Google. Use it to authenticate
                            // with your backend.
                            Log.d(TAG, "Got ID token.")
                            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                            Log.e("token", idToken)
                            auth.signInWithCredential(firebaseCredential)
                                .addOnCompleteListener(this) { task ->
                                    if (task.isSuccessful) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d(TAG, "signInWithCredential:success")
                                        // Send user to home screen
                                        Toast.makeText(this, "Welcome back", Toast.LENGTH_SHORT).show()
                                        loginSuccess = true
                                        onBackPressed()
                                    }
                                    else {
                                        binding.progressBar.visibility = View.GONE
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                                        task.exception?.message?.let {
                                            Snackbar.make(binding.googleIc,
                                                it, Snackbar.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                        }
                        password != null -> {
                            // Got a saved username and password. Use them to authenticate
                            // with your backend.
                            Log.d(TAG, "Got password.")
                            loginWithEmail(username, password)
                        }
                        else -> {
                            // Shouldn't happen.
                            Snackbar.make(binding.googleIc, "Something wrong happened!", Snackbar.LENGTH_INDEFINITE).show()
                            Log.d(TAG, "No ID token or password!")
                            binding.progressBar.visibility = View.GONE
                        }
                    }
                }catch (e: ApiException){
                    when (e.statusCode) {
                        CommonStatusCodes.CANCELED -> {
                            Log.d(TAG, "One-tap dialog was closed.")
                            // Don't re-prompt the user.
                            oneTapClient.beginSignIn(signUpRequest)
                                .addOnSuccessListener(this) { res ->
                                    signUpLauncher.launch(
                                        IntentSenderRequest.Builder(res.pendingIntent.intentSender).build()
                                    )
                                }
                                .addOnFailureListener(this) { error ->
                                    // No Google Accounts found. Just continue presenting the signed-out UI.
                                    error.localizedMessage?.let {
                                        Snackbar.make(binding.googleIc, it, Snackbar.LENGTH_INDEFINITE).show()
                                    }
                                    showOneTap = false
                                    binding.progressBar.visibility = View.GONE
                                }
                        }
                        CommonStatusCodes.NETWORK_ERROR -> {
                            Log.d(TAG, "One-tap encountered a network error.")
                            // Try again or just ignore.
                            Snackbar.make(binding.googleIc, "One-tap encountered a network error.", Snackbar.LENGTH_SHORT).show()
                            binding.progressBar.visibility = View.GONE
                        }
                        else -> {
                            Log.d(
                                TAG, "Couldn't get credential from result." +
                                    " (${e.localizedMessage})")
                            Snackbar.make(binding.googleIc, "Couldn't get credential from result." +
                                    " (${e.localizedMessage})", Snackbar.LENGTH_SHORT).show()
                            binding.progressBar.visibility = View.GONE
                        }
                    }
                }
            }
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = ActivityLoginBinding.inflate(layoutInflater)
            setContentView(binding.root)

            auth = Firebase.auth

            //Init Google SignUp SignIn Intent
            initGoogleSignIn()
            initGoogleSignUp()
            //Init Facebook SignIn
            initFacebook()

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
                //If user declined or no google account or password found. Don't prompt again
                if(showOneTap)
                    loginWithGoogle()
                else{
                    Snackbar.make(it, "No credentials found, try different login approach", Snackbar.LENGTH_INDEFINITE).show()
                }
            }

            binding.facebookIc.setOnClickListener {
                binding.progressBar.visibility = View.VISIBLE
                Log.d(TAG, "loginWithPermission")
                LoginManager.getInstance().logInWithReadPermissions(
                    this,
                    callbackManager,
                    listOf("email", "public_profile"))
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

        private fun initFacebook() {
            Log.d(TAG, "init")
            callbackManager = CallbackManager.Factory.create()
            LoginManager.getInstance().registerCallback(callbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(result: LoginResult) {
                        // App code
                        Log.d(TAG, "success")
                        handleFacebookAccessToken(result.accessToken)
                    }
                    override fun onCancel() {
                        // App code
                        Log.d(TAG, "cancel")
                        binding.progressBar.visibility = View.GONE
                    }

                    override fun onError(error: FacebookException) {
                        // App code
                        Log.d(TAG, "error")
                        binding.progressBar.visibility = View.GONE
                        error.localizedMessage?.let {
                            Snackbar.make(binding.facebookIc, it, Snackbar.LENGTH_SHORT)
                                .show()
                        }
                    }
                })
        }
        private fun handleFacebookAccessToken(accessToken: AccessToken) {
            Log.d(TAG, "handleFacebookAccessToken:$accessToken")
            val credential = FacebookAuthProvider.getCredential(accessToken.token)
            auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success")
                        val fbUser = auth.currentUser
                        verifyWithFirebase(fbUser, task)
                    } else {
                        binding.progressBar.visibility = View.GONE
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                        Snackbar.make(binding.facebookIc, "Facebook Authentication failed...",
                            Snackbar.LENGTH_SHORT).show()
                    }
                }
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

        private fun loginWithGoogle() {
            binding.progressBar.visibility = View.VISIBLE
            oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(this) { result ->
                    signInLauncher.launch(
                        IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                    )
                }
                .addOnFailureListener(this) { e ->
                    // No saved credentials found. Launch the One Tap sign-up flow, or
                    // do nothing and continue presenting the signed-out UI.
                    e.localizedMessage?.let {
                        Snackbar.make(binding.googleIc, it, Snackbar.LENGTH_SHORT).show()
                    }
                    oneTapClient.beginSignIn(signUpRequest)
                        .addOnSuccessListener(this) { res ->
                            signUpLauncher.launch(
                                IntentSenderRequest.Builder(res.pendingIntent.intentSender).build()
                            )
                        }
                        .addOnFailureListener(this) { error ->
                            // No Google Accounts found. Just continue presenting the signed-out UI.
                            error.localizedMessage?.let {
                                Snackbar.make(binding.googleIc, it, Snackbar.LENGTH_SHORT).show()
                            }
                            showOneTap = false
                            binding.progressBar.visibility = View.GONE
                        }
                }
        }

        private fun initGoogleSignUp() {
            signUpRequest =
                BeginSignInRequest.builder()
                    .setGoogleIdTokenRequestOptions(
                        BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                            .setSupported(true)
                            // Your server's client ID, not your Android client ID.
                            .setServerClientId(getString(R.string.default_web_client_id))
                            // Show all accounts on the device.
                            .setFilterByAuthorizedAccounts(false)
                            .build()
                    )
                    .build()
        }

        private fun initGoogleSignIn() {
            oneTapClient = Identity.getSignInClient(this)
            signInRequest = BeginSignInRequest.builder()
                .setPasswordRequestOptions(
                    BeginSignInRequest.PasswordRequestOptions.builder()
                        .setSupported(true)
                        .build()
                )
                .setGoogleIdTokenRequestOptions(
                    BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(getString(R.string.default_web_client_id))
                        .setFilterByAuthorizedAccounts(true)
                        .build()
                )
                .build()
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
