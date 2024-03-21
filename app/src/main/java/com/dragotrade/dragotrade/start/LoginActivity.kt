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
import com.dragotrade.dragotrade.databinding.ActivityLoginBinding
import com.dragotrade.dragotrade.start.fotgot_verification.EmailVerificationActivity
import com.dragotrade.dragotrade.start.fotgot_verification.EmailVerifySendActivity
import com.dragotrade.dragotrade.utils.Constants
import com.dragotrade.dragotrade.utils.LoadingBar
import com.dragotrade.dragotrade.utils.PreferenceManager
import com.dragotrade.dragotrade.utils.Validation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityLoginBinding
    private var passwordVisible = false
    private lateinit var preferenceManager: PreferenceManager

    private lateinit var firestore : FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userUID : String
    private var loadingBar = LoadingBar(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        preferenceManager = PreferenceManager.getInstance(this)

        setListener()

    }

    private fun setListener() {
        showPassword(binding.edPassword)
        binding.signupText.setOnClickListener(this)
        binding.loginButton.setOnClickListener(this)
        binding.forgotText.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.signup_text -> goToSignup(0)
            R.id.forgot_text -> goToSignup(1)
            R.id.login_button -> validates()
            else -> Toast.makeText(this@LoginActivity, "Default", Toast.LENGTH_SHORT).show()
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
                login(edEmail.text.toString(),edPassword.text.toString())
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
                       savePrefData(user)
                    }else{
                        loadingBar.HideDialog()
                        startActivity(Intent(this, EmailVerificationActivity::class.java))
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("TAG", "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    loadingBar.HideDialog()
                }
            }
    }

    private fun savePrefData(user: FirebaseUser) {
       userUID = user.uid
        firestore.collection(Constants.COLLECTION_USER)
            .document(userUID)
            .get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()){
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

                    startActivity(Intent(this,MainActivity::class.java))
                    finish()
                }else{
                    loadingBar.HideDialog()
                    Toast.makeText(this, "something went wrong", Toast.LENGTH_SHORT).show()
                }
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
}