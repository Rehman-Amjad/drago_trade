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
import com.dragotrade.dragotrade.R
import com.dragotrade.dragotrade.databinding.ActivitySignupBinding
import com.dragotrade.dragotrade.notification.CustomNotification
import com.dragotrade.dragotrade.start.fotgot_verification.EmailVerificationActivity
import com.dragotrade.dragotrade.utils.Constants
import com.dragotrade.dragotrade.utils.CurrentDateTime
import com.dragotrade.dragotrade.utils.LoadingBar
import com.dragotrade.dragotrade.utils.Validation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import java.sql.Timestamp
import java.util.Collections
import kotlin.random.Random

class SignupActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivitySignupBinding

    private lateinit var firestore : FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userUID : String
    private var currentDateTime = CurrentDateTime(this)
    private var customNotification = CustomNotification(this)
    private var loadingBar = LoadingBar(this)
    private var passwordVisible = false
    private var confirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        userUID = firebaseAuth.currentUser?.uid.toString()

        setListener()
    }

    @SuppressLint("SetTextI18n")
    private fun setListener() {
        showPassword(binding.edPassword)
        showConfirmPassword(binding.edConfirmPassword)
        binding.tvUsername.text = "Referral ID: " + generateRandomReferralCode()
        binding.signupButton.setOnClickListener(this)
    }

    private fun showMessage(message: String) {
        Toast.makeText(this@SignupActivity, message, Toast.LENGTH_SHORT).show()
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.signup_button -> validates()
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
                   saveUserData(
                       userUID,
                       fullName,
                       username,
                       email,
                       password,
                       referralCode,
                       result
                   )
               } else {
                   // If sign in fails, display a message to the user.
                   Log.w("TAG", "createUserWithEmail:failure", task.exception)
                   Toast.makeText(baseContext, "Already exits", Toast.LENGTH_SHORT,
                   ).show()
                   loadingBar.HideDialog()
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
            Constants.KEY_EMAIL to email,
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
        val map = hashMapOf<String,Any>(
            Constants.KEY_BALANCE to "0",
            Constants.KEY_WALLET_DAILY_EARNINGS to "0",
            Constants.KEY_WALLET_REFERRAL_EARNINGS to "0",
            Constants.KEY_WALLET_TOTAL_EARNINGS to "0",
            Constants.KEY_WALLET_TOTAL_WITHDRAW to "0"

        )
       firestore.collection(Constants.COLLECTION_WALLET).document(userUID)
           .set(map).addOnCompleteListener {
               if (it.isSuccessful){
                  sendEmailLink()
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