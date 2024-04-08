package com.dragotrade.dragotrade.start

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.dragotrade.dragotrade.MainActivity
import com.dragotrade.dragotrade.databinding.ActivitySplashBinding
import com.dragotrade.dragotrade.start.onboard.Onboard1Activity
import com.dragotrade.dragotrade.start.onboard.Onboard2Activity
import com.dragotrade.dragotrade.utils.Constants
import com.google.firebase.dynamiclinks.PendingDynamicLinkData
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getRefClink()

    }

    private fun getRefClink() {
        Firebase.dynamicLinks
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData: PendingDynamicLinkData? ->
                // Get deep link from result (may be null if no link is found)
                var deepLink: Uri? = null
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                    Log.d("DynamicLink", "getRefClink: "+deepLink)

                    try {
                        var getUsername = deepLink.toString()
                        getUsername = getUsername.substring(getUsername.lastIndexOf("=")+1)
                        Log.d("DynamicLink", "getRefClink Username: $getUsername")
                        delayFun("signup",getUsername.toString())
                    }catch (e: IllegalStateException) {
                        Log.d("DynamicLink", "getRefClink: "+e.message)
                    }



                }else{
                    delayFun("login","")
                }

                // Handle the deep link. For example, open the linked
                // content, or apply promotional credit to the user's
                // account.
                // ...
            }
            .addOnFailureListener(this) { e -> Log.w("DynamicLink", "getDynamicLink:onFailure", e) }
    }

    private fun delayFun(activity: String, referral: String) {
        Handler(Looper.getMainLooper()).postDelayed({
            if (activity == "login"){
                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }else if (activity == "signup"){
                val intent = Intent(this@SplashActivity, SignupActivity::class.java)
                intent.putExtra(Constants.INTENT_KEY_LINK_REF,referral)
                startActivity(intent)
            }
        },3000)
    }

}

