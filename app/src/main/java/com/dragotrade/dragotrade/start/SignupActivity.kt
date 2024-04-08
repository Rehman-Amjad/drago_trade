package com.dragotrade.dragotrade.start

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.dragotrade.dragotrade.MainActivity
import com.dragotrade.dragotrade.R
import com.dragotrade.dragotrade.databinding.ActivitySignupBinding
import com.dragotrade.dragotrade.notification.CustomNotification
import com.dragotrade.dragotrade.start.fotgot_verification.EmailVerificationActivity
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

class SignupActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var preferenceManager: PreferenceManager

    private lateinit var firestore : FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userUID : String
    private var currentDateTime = CurrentDateTime(this)
    private var customNotification = CustomNotification(this)
    private var loadingBar = LoadingBar(this)
    private var passwordVisible = false
    private var confirmPasswordVisible = false
    private var loginWith : String = "Google"

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var signInRequest : BeginSignInRequest
    companion object {
        private const val RC_SIGN_IN = 123
        private const val TAG = "GoogleActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        userUID = firebaseAuth.currentUser?.uid.toString()
        preferenceManager = PreferenceManager.getInstance(this)

        createRequest()
        setListener()
    }

    @SuppressLint("SetTextI18n")
    private fun setListener() {
        showPassword(binding.edPassword)
        showConfirmPassword(binding.edConfirmPassword)
        binding.tvUsername.text = "Referral ID: " + generateRandomReferralCode()
        binding.signupButton.setOnClickListener(this)
        binding.googleButton.setOnClickListener(this)
    }

    private fun showMessage(message: String) {
        Toast.makeText(this@SignupActivity, message, Toast.LENGTH_SHORT).show()
    }
    private fun createRequest() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
    }


    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.signup_button -> validates()
            R.id.google_button -> signIn()
        }
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, LoginActivity.RC_SIGN_IN)
    }
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == LoginActivity.RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account?.idToken)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(LoginActivity.TAG, "Google sign in failed", e)
                // ...
            }
        }
    }
    private fun firebaseAuthWithGoogle(idToken: String?) {
        loadingBar.ShowDialog("Please Wait...")
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    loadingBar.ShowDialog("Please Wait...")
                    updateUI()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(LoginActivity.TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this@SignupActivity, "Authentication Failed.", Toast.LENGTH_SHORT).show()

                }
            }
    }
    private fun updateUI() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let { currentUser ->
            val userUID = currentUser.uid
            val userRef = FirebaseFirestore.getInstance().collection(Constants.COLLECTION_USER).document(userUID)

            userRef.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val snapshot = task.result
                    if (snapshot != null && snapshot.exists()) {
                        // User exists, proceed to MainActivity
                        loadingBar.HideDialog()
                        savePrefDataGoogle(user)
                        Toast.makeText(this,"Account Already exist.", Toast.LENGTH_SHORT).show()
                    } else {
                        VerificationAndDataSave(currentUser)
                    }
                } else {
                    // Handle failure
                }
            }
        }
    }

    private fun VerificationAndDataSave(user: FirebaseUser) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val tokenResult = task.result
                val isEmailVerified = user.isEmailVerified

                if (isEmailVerified) {
                    savePrefDataGoogle(user)
                } else {
                    loadingBar.HideDialog()
                    startActivity(Intent(this, EmailVerificationActivity::class.java))
                }

                // Proceed with saving user data
                user.uid?.let { saveData(
                    it,
                    tokenResult,
                    binding.tvUsername.text.toString().substring(13, 18)
                ) }
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
                    addWalletGoogle()

                    customNotification.ShowNotification(R.drawable.logo,"your account has been created successfully")
                    customNotification.saveNotification(userUID,"Account Creation","Thanks for creating account")

                    loadingBar.HideDialog()
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
    private fun savePrefDataGoogle(user: FirebaseUser) {
        val userUID = user.uid
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

                    startActivity(Intent(this@SignupActivity, MainActivity::class.java))
                    finish()

                } else {
                    // Handle the case where user data doesn't exist
                    loadingBar.HideDialog()
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun validates() {
        binding.apply {
           if (edFullName.text.isNullOrEmpty()){
               showMessage(getString(R.string.full_name))
           }else if (edEmail.text.isNullOrEmpty()){
               showMessage(getString(R.string.email))
           }else if (!Validation.isEmailValid(edEmail.text.toString())) {
               showMessage(getString(R.string.email_not_valid))
           }else if (edPassword.text.isNullOrEmpty()) {
               showMessage(getString(R.string.enter_password))
           } else if (!Validation.isPasswordValid(edPassword.text.toString())) {
               showMessage(getString(R.string.password_length))
           } else if (edConfirmPassword.text.isNullOrEmpty()) {
               showMessage(getString(R.string.enter_confirm_password))
           }else {
               loadingBar.ShowDialog("Please wait")
               FirebaseMessaging.getInstance().token.addOnCompleteListener {
                   if (!it.isSuccessful) {
                       return@addOnCompleteListener
                   }
                   registerUserToFirebase(
                       edFullName.text.toString(),
                       tvUsername.text.toString().substring(13, 18),
                       edEmail.text.toString(),
                       edPassword.text.toString(),
                       edReferralCode.text.toString(),
                       it.result,
                   )
               }
           }
        }
    }

   private fun registerUserToFirebase(
    fullName : String,username : String, email : String, password : String, referralCode : String,
    result : String
    ){
       firebaseAuth.createUserWithEmailAndPassword(email, password)
           .addOnCompleteListener(this) { task ->
               if (task.isSuccessful) {
                   // Sign in success, update UI with the signed-in user's information

                   userUID = firebaseAuth.currentUser?.uid.toString()

                   checkUserDocument(userUID,
                       fullName,
                       username,
                       email,
                       password,
                       referralCode,
                       result)

               } else {
                   // If sign in fails, display a message to the user.
                   Log.w("TAG", "createUserWithEmail:failure", task.exception)
                   Toast.makeText(baseContext, "Already exits", Toast.LENGTH_SHORT,
                   ).show()
                   loadingBar.HideDialog()
               }
           }
    }

    private fun checkUserDocument(
        userUID: String, fullName: String,
        username : String,
        email: String, password: String,
        referralCode: String, result: String
    ) {
        if (userUID != null) {
            val userRef = FirebaseFirestore.getInstance().collection(Constants.COLLECTION_USER).document(userUID)
            userRef.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null && document.exists()) {

                        Toast.makeText(this,"Account Already Exist", Toast.LENGTH_SHORT).show()

                    } else {
                        saveUserData(
                            userUID,
                            fullName,
                            username,
                            email,
                            password,
                            referralCode,
                            result
                        )
                    }
                } else {
                    Log.d(TAG, "Failed with: ", task.exception)
                    // Handle the failure here
                }
            }
        }
    }


    private fun saveUserData(userUID: String, fullName: String,
                             username : String,
                             email: String, password: String,
                             referralCode: String, result: String) {
        val currentTimestamp = System.currentTimeMillis()
        val map = hashMapOf<String,Any>(
            Constants.KEY_USERUID to userUID,
            Constants.KEY_FULLNAME to fullName,
            Constants.KEY_USERNAME to username,
            Constants.KEY_EMAIL to email.lowercase(),
            Constants.KEY_PASSWORD to password,
            Constants.KEY_REFERRAL_CODE to referralCode,
            Constants.KEY_TOKEN to result,
            Constants.KEY_DATE to currentDateTime.getCurrentDate().toString(),
            Constants.KEY_TIME to currentDateTime.getTimeWithAmPm().toString(),
            Constants.KEY_ACCOUNT_METHOD to "custom",
            Constants.KEY_TIMESTAMP to currentTimestamp.toString(),
            Constants.KEY_USER_VERIFIED to "0",
            Constants.KEY_PROFILE_PIC to "",
            Constants.KEY_PHONE_NUMBER to ""
        )
        firestore.collection(Constants.COLLECTION_USER).document(userUID).set(map)
            .addOnCompleteListener {
               if (it.isSuccessful){
                   addWallet()
                   customNotification.ShowNotification(R.drawable.logo,"your account has been created successfully")
                   customNotification.saveNotification(userUID,"Account Creation","Thanks for creating account")
                   loadingBar.HideDialog()
               }
            }

    }

    private fun addWallet() {
        val userUID = firebaseAuth.currentUser?.uid.toString()

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
                  sendEmailLink()
               }
           }
    }
    private fun addWalletGoogle() {
        val userUID = firebaseAuth.currentUser?.uid.toString()
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
                    Toast.makeText(this,"Wallet created", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@SignupActivity, MainActivity::class.java))
                    finish()
                }
            }
    }

    private fun sendEmailLink() {
       firebaseAuth.currentUser?.sendEmailVerification()?.addOnCompleteListener{task ->
           if (task.isSuccessful){
               startActivity(Intent(this,EmailVerificationActivity::class.java))
               finish()
           }
       }?.addOnFailureListener {
           Toast.makeText(this,it.message.toString(), Toast.LENGTH_SHORT).show()
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

    @SuppressLint("ClickableViewAccessibility")
    private fun showConfirmPassword(password: EditText) {
        password.setOnTouchListener { _, event ->
            val drawableEnd = 2 // Index of the drawable end
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= password.right - password.compoundDrawables[drawableEnd].bounds.width()) {
                    // Toggle password visibility
                    if (confirmPasswordVisible){
                        password.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.ic_visibility_off,0)
                        password.transformationMethod = PasswordTransformationMethod()
                        confirmPasswordVisible = false
                    }else{
                        password.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.ic_visibility_on,0)
                        password.transformationMethod = null
                        confirmPasswordVisible = true
                    }
                }
            }
            false
        }
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