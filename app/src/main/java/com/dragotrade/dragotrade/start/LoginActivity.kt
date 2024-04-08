package com.dragotrade.dragotrade.start

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dragotrade.dragotrade.MainActivity
import com.dragotrade.dragotrade.R
import com.dragotrade.dragotrade.databinding.ActivityLoginBinding
import com.dragotrade.dragotrade.start.fotgot_verification.EmailVerificationActivity
import com.dragotrade.dragotrade.start.fotgot_verification.EmailVerifySendActivity
import com.dragotrade.dragotrade.utils.Constants
import com.dragotrade.dragotrade.utils.CurrentDateTime
import com.dragotrade.dragotrade.utils.LoadingBar
import com.dragotrade.dragotrade.utils.PreferenceManager
import com.dragotrade.dragotrade.utils.Validation
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlin.random.Random


class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityLoginBinding
    private var passwordVisible = false
    private lateinit var preferenceManager: PreferenceManager

    private lateinit var firestore : FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userUID : String
    private var loginWith : String = "Google"
    private var loadingBar = LoadingBar(this)
    private var currentDateTime = CurrentDateTime(this)

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var signInRequest : BeginSignInRequest
    companion object {
        const val RC_SIGN_IN = 123
        const val TAG = "GoogleActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        preferenceManager = PreferenceManager.getInstance(this)

        createRequest()
        setListener()


    }

    private fun createRequest() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setListener() {
        showPassword(binding.edPassword)
        binding.tvUsername.text = "Referral ID: " + generateRandomReferralCode()
        binding.signupText.setOnClickListener(this)
        binding.loginButton.setOnClickListener(this)
        binding.forgotText.setOnClickListener(this)
        binding.googleButton.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.signup_text -> goToSignup(0)
            R.id.forgot_text -> goToSignup(1)
            R.id.login_button -> validates()
            R.id.google_button -> signIn()
            else -> Toast.makeText(this@LoginActivity, "Default", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account?.idToken)
            } catch (e: ApiException) {

                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }
    private fun firebaseAuthWithGoogle(idToken: String?) {
        loadingBar.ShowDialog("Please Wait...")
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    userUID = firebaseAuth.currentUser?.uid.toString()

                    updateUI(userUID)
//                    Toast.makeText(this@LoginActivity, userUID, Toast.LENGTH_SHORT).show()

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this@LoginActivity, "Authentication Failed1.", Toast.LENGTH_SHORT).show()

                }
            }
    }

    private fun updateUI(userUID: String) {
        val userRef = FirebaseFirestore.getInstance().collection(Constants.COLLECTION_USER).document(userUID)

        userRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val snapshot = task.result
                if (snapshot != null && snapshot.exists()) {
                    loadingBar.HideDialog()
                    savePrefDataGoogle(userUID)
                    Toast.makeText(
                        baseContext,
                        "Account already exists.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    VerificationAndDataSave(userUID)
                }
            } else {
                // Handle failure
            }
        }
    }


    private fun VerificationAndDataSave(userUID: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val tokenResult = task.result

                FirebaseAuth.getInstance().currentUser?.reload()?.addOnCompleteListener { reloadTask ->
                    if (reloadTask.isSuccessful) {
                        val currentUser = FirebaseAuth.getInstance().currentUser
                        val isEmailVerified = currentUser?.isEmailVerified ?: false

                        savePrefDataGoogle(userUID)
                        saveData(userUID,
                            tokenResult,
                            binding.tvUsername.text.toString().substring(13, 18))

                    } else {
                        Log.d(TAG, "Failed to reload user: ", reloadTask.exception)
                        loadingBar.HideDialog()
                    }
                }
            }
        }
    }






    private fun saveData(userUID: String, result: String, userName: String) {
        loadingBar.ShowDialog("Please Wait...")
        val currentTimestamp = System.currentTimeMillis()

        val acct = GoogleSignIn.getLastSignedInAccount(this)
        val profileImageLink: String?

        val map = hashMapOf<String, Any>(
            Constants.KEY_FULLNAME to (acct?.displayName ?: ""),
            Constants.KEY_EMAIL to (acct?.email ?: ""),
            Constants.KEY_USERUID to userUID,
            Constants.KEY_USERNAME to userName,
            Constants.KEY_REFERRAL_CODE to "",
            Constants.KEY_TOKEN to result,
            Constants.KEY_DATE to currentDateTime.getCurrentDate().toString(),
            Constants.KEY_TIME to currentDateTime.getTimeWithAmPm().toString(),
            Constants.KEY_ACCOUNT_METHOD to loginWith,
            Constants.KEY_TIMESTAMP to currentTimestamp.toString(),
            Constants.KEY_USER_VERIFIED to "0",
            Constants.KEY_PROFILE_PIC to (acct?.photoUrl?.toString() ?: ""),
            Constants.KEY_PHONE_NUMBER to ""
        )

        FirebaseFirestore.getInstance().collection(Constants.COLLECTION_USER).document(userUID)
            .set(map)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    loadingBar.HideDialog()
                    addWallet(userUID)

                }
            }
            .addOnFailureListener { e ->
                loadingBar.HideDialog()
                Toast.makeText(
                    baseContext,
                    "Something Went Wrong.",
                    Toast.LENGTH_SHORT,
                ).show()
            }

    }

    private fun addWallet(userUID: String) {
        val map = hashMapOf<String,Any>(
            Constants.KEY_BALANCE to 0,
            Constants.KEY_WALLET_DAILY_EARNINGS to 0,
            Constants.KEY_WALLET_REFERRAL_EARNINGS to 0,
            Constants.KEY_WALLET_TOTAL_EARNINGS to 0,
            Constants.KEY_WALLET_TOTAL_WITHDRAW to 0,
            Constants.KEY_USERUID to userUID

        )
        firestore.collection(Constants.COLLECTION_WALLET).document(userUID)
            .set(map).addOnCompleteListener {
                if (it.isSuccessful){
                    Toast.makeText(this, "Wallet Created", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                }
            }
    }
    private fun validates() {
        binding.apply {
            if (edEmail.text.isNullOrEmpty()){
                showMessage(getString(R.string.email))
            }else if (!Validation.isEmailValid(edEmail.text.toString())) {
                showMessage(getString(R.string.email_not_valid))
            }else if (edPassword.text.isNullOrEmpty()) {
                showMessage(getString(R.string.enter_password))
            } else{
                loadingBar.ShowDialog("please wait")
                login(edEmail.text.toString(),
                    edPassword.text.toString())
            }
        }
    }

    private fun login(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = firebaseAuth.currentUser
                    if (user?.isEmailVerified == true){
                       savePrefData(user,password)
                    }else{
                        loadingBar.HideDialog()
                        startActivity(Intent(this, EmailVerificationActivity::class.java))
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("TAG", "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.3",
                        Toast.LENGTH_SHORT,
                    ).show()
                    retrieveStoredPassword()
                    loadingBar.HideDialog()
                }
            }
    }

    private fun savePrefData(user: FirebaseUser, enteredPassword: String) {
       userUID = user.uid
        firestore.collection(Constants.COLLECTION_USER)
            .document(userUID)
            .get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()){
                    val storedPassword = snapshot.getString(Constants.KEY_PASSWORD).toString()
                    if (storedPassword == enteredPassword) {
                        // Password matches, update other preferences
                        preferenceManager.putString(Constants.KEY_FULLNAME, snapshot.getString(Constants.KEY_FULLNAME).toString())
                        preferenceManager.putString(Constants.KEY_USERNAME, snapshot.getString(Constants.KEY_USERNAME).toString())
                        preferenceManager.putString(Constants.KEY_USERUID, snapshot.getString(Constants.KEY_USERUID).toString())
                        preferenceManager.putString(Constants.KEY_EMAIL, snapshot.getString(Constants.KEY_EMAIL).toString())
                        preferenceManager.putString(Constants.KEY_USER_VERIFIED, snapshot.getString(Constants.KEY_USER_VERIFIED).toString())
                        preferenceManager.putString(Constants.KEY_TOKEN, snapshot.getString(Constants.KEY_TOKEN).toString())
                        preferenceManager.putString(Constants.KEY_TIMESTAMP, snapshot.getString(Constants.KEY_TIMESTAMP).toString())
                        preferenceManager.putString(Constants.KEY_TIME, snapshot.getString(Constants.KEY_TIME).toString())
                        preferenceManager.putString(Constants.KEY_DATE, snapshot.getString(Constants.KEY_DATE).toString())
                        preferenceManager.putString(Constants.KEY_REFERRAL_CODE, snapshot.getString(Constants.KEY_REFERRAL_CODE).toString())
                        preferenceManager.putString(Constants.KEY_ACCOUNT_METHOD, snapshot.getString(Constants.KEY_ACCOUNT_METHOD).toString())
                        preferenceManager.putString(Constants.KEY_PASSWORD, snapshot.getString(Constants.KEY_PASSWORD).toString())
                        preferenceManager.putString(Constants.KEY_PROFILE_PIC, snapshot.getString(Constants.KEY_PROFILE_PIC).toString())
                        preferenceManager.putString(Constants.KEY_PHONE_NUMBER, snapshot.getString(Constants.KEY_PHONE_NUMBER).toString())


                        // Proceed with login
                        startActivity(Intent(this,MainActivity::class.java))
                        finish()
                    } else {
                        // Password doesn't match, update the stored password
                        firestore.collection(Constants.COLLECTION_USER)
                            .document(userUID)
                            .update(Constants.KEY_PASSWORD, enteredPassword)
                            .addOnSuccessListener {
                                // Update successful, proceed with login
                                preferenceManager.putString(Constants.KEY_FULLNAME, snapshot.getString(Constants.KEY_FULLNAME).toString())
                                // Update other preferences as needed
                                startActivity(Intent(this,MainActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener { exception ->
                                // Handle update failure
                                loadingBar.HideDialog()
                                Toast.makeText(this, "Failed to update password: ${exception.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    loadingBar.HideDialog()
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun savePrefDataGoogle(userUID: String) {
        firestore.collection(Constants.COLLECTION_USER)
            .document(userUID)
            .get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    // Update preferences with user data
                    preferenceManager.putString(Constants.KEY_FULLNAME, snapshot.getString(Constants.KEY_FULLNAME).toString())
                    preferenceManager.putString(Constants.KEY_USERNAME, snapshot.getString(Constants.KEY_USERNAME).toString())
                    preferenceManager.putString(Constants.KEY_USERUID, snapshot.getString(Constants.KEY_USERUID).toString())
                    preferenceManager.putString(Constants.KEY_EMAIL, snapshot.getString(Constants.KEY_EMAIL).toString())
                    preferenceManager.putString(Constants.KEY_USER_VERIFIED, snapshot.getString(Constants.KEY_USER_VERIFIED).toString())
                    preferenceManager.putString(Constants.KEY_TOKEN, snapshot.getString(Constants.KEY_TOKEN).toString())
                    preferenceManager.putString(Constants.KEY_TIMESTAMP, snapshot.getString(Constants.KEY_TIMESTAMP).toString())
                    preferenceManager.putString(Constants.KEY_TIME, snapshot.getString(Constants.KEY_TIME).toString())
                    preferenceManager.putString(Constants.KEY_DATE, snapshot.getString(Constants.KEY_DATE).toString())
                    preferenceManager.putString(Constants.KEY_REFERRAL_CODE, snapshot.getString(Constants.KEY_REFERRAL_CODE).toString())
                    preferenceManager.putString(Constants.KEY_ACCOUNT_METHOD, snapshot.getString(Constants.KEY_ACCOUNT_METHOD).toString())
                    preferenceManager.putString(Constants.KEY_PASSWORD, snapshot.getString(Constants.KEY_PASSWORD).toString())
                    preferenceManager.putString(Constants.KEY_PROFILE_PIC, snapshot.getString(Constants.KEY_PROFILE_PIC).toString())
                    preferenceManager.putString(Constants.KEY_PHONE_NUMBER, snapshot.getString(Constants.KEY_PHONE_NUMBER).toString())

                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()

                } else {
                    // Handle the case where user data doesn't exist
                    loadingBar.HideDialog()
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
            }
    }


    private fun retrieveStoredPassword() {
        val email = binding.edEmail.text.toString().trim()
        val enteredPassword = binding.edPassword.text.toString().trim()

        firestore.collection(Constants.COLLECTION_USER)
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val storedPassword = querySnapshot.documents[0].getString(Constants.KEY_PASSWORD).toString()
                    if (storedPassword != enteredPassword) {
                        showMessage("Wrong Password")
                    }
                } else {
                    showMessage("Email not registered")
                }
            }
            .addOnFailureListener { exception ->
                showMessage("Failed to retrieve user data: ${exception.message}")
            }
    }


    private fun goToSignup(number: Int) {
        if (number==0){
            startActivity(Intent(this, SignupActivity::class.java))
        }else{
            startActivity(Intent(this, EmailVerifySendActivity::class.java))
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showPassword(password: EditText) {
        password.setOnTouchListener { _, event ->
            val drawableEnd = 2 // Index of the drawable end
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= password.right - password.compoundDrawables[drawableEnd].bounds.width()) {
                    // Toggle password visibility
                    if (passwordVisible){
                        password.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.ic_visibility_off,0)
                        password.transformationMethod = PasswordTransformationMethod()
                        passwordVisible = false
                    }else{
                        password.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.ic_visibility_on,0)
                        password.transformationMethod = null
                        passwordVisible = true
                    }
                }
            }
            false
        }
    }
    private fun showMessage(message: String) {
        Toast.makeText(this@LoginActivity, message, Toast.LENGTH_SHORT).show()
    }
    private fun generateRandomReferralCode(): String {
        val characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        val random = Random

        // Generate a random 6-character string

        return (1..6)
            .map { characters[random.nextInt(0, characters.length)] }
            .joinToString("")
    }
}