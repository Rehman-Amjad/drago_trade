package com.dragotrade.dragotrade.profile

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.dragotrade.dragotrade.R
import com.dragotrade.dragotrade.databinding.ActivityEditProfileBinding
import com.dragotrade.dragotrade.utils.Constants
import com.dragotrade.dragotrade.utils.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.UUID

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding:ActivityEditProfileBinding
    private lateinit var preferenceManager: PreferenceManager

    private lateinit var imgUri : Uri
    private lateinit var url : String
    private lateinit var firestore : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        preferenceManager = PreferenceManager.getInstance(this)

        binding.name.text = preferenceManager.getString(Constants.KEY_FULLNAME)
        binding.email.text = preferenceManager.getString(Constants.KEY_EMAIL)
        binding.password.text = preferenceManager.getString(Constants.KEY_PASSWORD)
        binding.edPhone.setText(preferenceManager.getString(Constants.KEY_PHONE_NUMBER))

        if (preferenceManager.getString(Constants.KEY_PROFILE_PIC)!=null &&
            preferenceManager.getString(Constants.KEY_PROFILE_PIC) != "")
        {
            Glide.with(this)
                .load(preferenceManager.getString(Constants.KEY_PROFILE_PIC))
                .into(binding.profileImage)
        }
        binding.backLayout.backImage.setOnClickListener{
            onBackPressedDispatcher.onBackPressed()
        }
        binding.backLayout.backText.text = "Update Profile"

        binding.profileImage.setOnClickListener {
            val pickImg = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            changeImage.launch(pickImg)
        }

        binding.updateButton.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            binding.updateButton.visibility = View.GONE
            updateProfile()
        }
    }

    private fun updateProfile() {
        val map = hashMapOf<String,Any>(
            Constants.KEY_PHONE_NUMBER to binding.edPhone.text.toString()
        )
        firestore.collection(Constants.COLLECTION_USER)
            .document(preferenceManager.getString(Constants.KEY_USERUID))
            .update(map).addOnCompleteListener{task->
                if (task.isSuccessful){
                    binding.progressBar.visibility = View.GONE
                    binding.updateButton.visibility = View.VISIBLE
                    preferenceManager.putString(Constants.KEY_PHONE_NUMBER,binding.edPhone.text.toString())
                    Toast.makeText(this,"profile updated",Toast.LENGTH_SHORT).show()
                }
            }
    }

    private val changeImage =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                val data = it.data
                imgUri = data?.data!!
                binding.profileImage.setImageURI(imgUri)
                uploadImage()
            }
        }

    fun uploadImage(){
        if(imgUri!=null){
            val progressDialog = ProgressDialog(this)
            progressDialog.setTitle("Uploading...")
            progressDialog.setMessage("Uploading your image..")
            progressDialog.show()

            val ref : StorageReference = FirebaseStorage.getInstance().reference
                .child(UUID.randomUUID().toString())
            ref.putFile(imgUri).addOnSuccessListener { taskSnapshot ->
                progressDialog.dismiss()
                taskSnapshot.storage.downloadUrl.addOnSuccessListener {
                    url = it.toString()
                    updateImageToDatabase()
                }


            }.addOnFailureListener{
                progressDialog.dismiss()
                Toast.makeText(this, "Fail to Upload Image..", Toast.LENGTH_SHORT)
                    .show()
            }

        }
    }

    private fun updateImageToDatabase() {
        val map = hashMapOf<String,Any>(
            Constants.KEY_PROFILE_PIC to url
        )
        firestore.collection(Constants.COLLECTION_USER).document(preferenceManager.getString(Constants.KEY_USERUID))
            .update(map).addOnCompleteListener{
                if (it.isSuccessful){
                    preferenceManager.putString(Constants.KEY_PROFILE_PIC,url)
                    Toast.makeText(this, "profile image updated...", Toast.LENGTH_SHORT)
                        .show()
                }
            }.addOnFailureListener{
                Toast.makeText(this, "something went wrong!!", Toast.LENGTH_SHORT)
                    .show()
            }

    }
}