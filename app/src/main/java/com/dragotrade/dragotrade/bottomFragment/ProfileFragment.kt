package com.dragotrade.dragotrade.bottomFragment

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.dragotrade.dragotrade.R
import com.dragotrade.dragotrade.databinding.FragmentProfileBinding
import com.dragotrade.dragotrade.profile.ContactActivity
import com.dragotrade.dragotrade.profile.EditProfileActivity
import com.dragotrade.dragotrade.profile.SupportActivity
import com.dragotrade.dragotrade.screens.chat.ChatActivity
import com.dragotrade.dragotrade.start.LoginActivity
import com.dragotrade.dragotrade.utils.Constants
import com.dragotrade.dragotrade.utils.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks

class ProfileFragment : Fragment(),View.OnClickListener {

    private lateinit var binding : FragmentProfileBinding
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding =  FragmentProfileBinding.inflate(layoutInflater, container, false)

       return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        inits()
    }

    @SuppressLint("SetTextI18n")
    private fun inits() {
        setListener()
        preferenceManager = PreferenceManager.getInstance(requireContext())
        binding.apply {
            fullName.text = preferenceManager.getString(Constants.KEY_FULLNAME)
            includeAccount.email.text = preferenceManager.getString(Constants.KEY_EMAIL)
            includeAccount.username.text = "ID: "+preferenceManager.getString(Constants.KEY_USERNAME)
        }
        if (preferenceManager.getString(Constants.KEY_PROFILE_PIC)!=null &&
            preferenceManager.getString(Constants.KEY_PROFILE_PIC) != "")
        {
            Glide.with(this)
                .load(preferenceManager.getString(Constants.KEY_PROFILE_PIC))
                .into(binding.profileImage)
        }
    }

    private fun setListener() {
        binding.includeAccount.llUpdateProfile.setOnClickListener(this)
        binding.includeAccount.usernameCopy.setOnClickListener(this)
        binding.includeAccount.linkCopy.setOnClickListener(this)
        binding.includeAccount.shareImage.setOnClickListener(this)

        binding.includeOption.llFaq.setOnClickListener(this)
        binding.includeOption.llLiveSupport.setOnClickListener(this)
        binding.includeOption.llContact.setOnClickListener(this)
        binding.includeOption.llLogout.setOnClickListener(this)


    }

    override fun onClick(v: View?) {
       when (v?.id) {
           R.id.ll_updateProfile -> moveScreen()
           R.id.username_copy-> copyClipBoard(preferenceManager.getString(Constants.KEY_USERNAME))
           R.id.link_copy->  generateRefLink()
           R.id.share_image->  generateRefLink()
           R.id.ll_logout -> logout()
           R.id.ll_faq -> startActivity(Intent(requireContext(),SupportActivity::class.java))
           R.id.ll_contact -> startActivity(Intent(requireContext(),ContactActivity::class.java))
           R.id.ll_liveSupport -> startActivity(Intent(requireContext(),ChatActivity::class.java))
       }
    }

    private fun logout() {
        Toast.makeText(requireActivity(), "Sign out", Toast.LENGTH_SHORT).show()
       FirebaseAuth.getInstance().signOut()
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    private fun moveScreen() {
        startActivity(Intent(requireContext(),EditProfileActivity::class.java))
    }

    private fun copyClipBoard(textToCopy: String) {
        val clipboardManager =
            requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", textToCopy)
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(requireActivity(), "Referral Code copied to clipboard", Toast.LENGTH_LONG)
            .show()
    }

    private fun generateRefLink() {

        val customLink = "${Constants.LINK_BASE_URL}?" +
                "link=${Constants.WEBSITE_LINK}?username=" + preferenceManager.getString(Constants.KEY_USERNAME) +
                "&apn=" + activity?.packageName +
                "&afl=${Constants.WEBSITE_LINK_DOWNLOAD_PAGE}" +
                "&efr=1"

        activity?.let {
            FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLongLink(Uri.parse(customLink))
                .buildShortDynamicLink()
                .addOnCompleteListener(it) { task ->
                    if (task.isSuccessful) {
                        // Short link created
                        val shortLink: Uri? = task.result?.shortLink
                        val flowchartLink: Uri? = task.result?.previewLink
                        Log.d("DynamicLink", "onCreate: $shortLink\n$flowchartLink")
                        copyClipBoard(shortLink.toString())
                        shareData(shortLink.toString())
                    } else {
                        // Error
                        // ...
                    }
                }
        }

    }

    private fun shareData(referralUrl: String) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Drago App Referral Link")
        shareIntent.putExtra(Intent.EXTRA_TEXT, referralUrl)
        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }

}