package com.dragotrade.dragotrade.screens.deposit

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.ProgressDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.dragotrade.dragotrade.R
import com.dragotrade.dragotrade.databinding.ActivityDepositBinding
import com.dragotrade.dragotrade.notification.CustomNotification
import com.dragotrade.dragotrade.utils.Constants
import com.dragotrade.dragotrade.utils.CurrentDateTime
import com.dragotrade.dragotrade.utils.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.UUID

class DepositActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityDepositBinding
    private lateinit var preferenceManager: PreferenceManager
    private var selectedImage: Uri = Uri.parse("")
    private var depositMethod : String = ""

    private lateinit var firestore: FirebaseFirestore
    private var customNotification  = CustomNotification(this)
    private var currentDateTime  = CurrentDateTime(this)

    private val PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDepositBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        init()
    }
    private fun init() {
        notification()
        checkPermissions()
        spinner()
        amountChange()
        preferenceManager = PreferenceManager.getInstance(this)
        setListeners()
    }

    private fun checkPermissions() {
        // Check if the permission is already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
        } else {
           // showMessage("Permission already granted")
        }
    }


    @SuppressLint("SetTextI18n")
    private fun setListeners() {
        binding.includeBack.backText.text = "Deposit"
        binding.includeBack.backImage.setOnClickListener(this)
        binding.uploadImageButton.setOnClickListener(this)
        binding.depositButton.setOnClickListener(this)

        binding.includeBack.backImage.setOnClickListener(this)
        binding.includeDepositInput.edAmount.setOnClickListener(this)

        // deposit account
        binding.includeDepositAmount.ivCopyORXclipboard.setOnClickListener(this)
        binding.includeDepositAmount.ivCopyclipboard.setOnClickListener(this)
        binding.includeDepositAmount.ivCopyPayidClipboard.setOnClickListener(this)

        //button price
        binding.includeDepositPriceButton.button50.setOnClickListener(this)
        binding.includeDepositPriceButton.button100.setOnClickListener(this)
        binding.includeDepositPriceButton.button150.setOnClickListener(this)
        binding.includeDepositPriceButton.button200.setOnClickListener(this)
        binding.includeDepositPriceButton.button250.setOnClickListener(this)
        binding.includeDepositPriceButton.button300.setOnClickListener(this)

    }

    @SuppressLint("SetTextI18n")
    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.back_image -> onBackPressedDispatcher.onBackPressed()
            R.id.iv_copyclipboard -> copyClipBoard(binding.includeDepositAmount.tvBinanceAddress)
            R.id.iv_copyPayidClipboard -> copyClipBoard(binding.includeDepositAmount.tvBpayid)
            R.id.iv_copyORXclipboard -> copyClipBoard(binding.includeDepositAmount.tvORXAddress)
            R.id.upload_image_button -> pickImageFromGallery()
            R.id.button_50 -> binding.includeDepositPriceButton.amount.text = "50"
            R.id.button_100 -> binding.includeDepositPriceButton.amount.text = "100"
            R.id.button_150 -> binding.includeDepositPriceButton.amount.text = "150"
            R.id.button_200 -> binding.includeDepositPriceButton.amount.text = "200"
            R.id.button_250 -> binding.includeDepositPriceButton.amount.text = "250"
            R.id.button_300 -> binding.includeDepositPriceButton.amount.text = "300"
            R.id.deposit_button -> deposit()
            else -> Toast.makeText(this, "Default", Toast.LENGTH_SHORT).show()
        }
    }

    private fun amountChange() {

        binding.includeDepositInput.edAmount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.includeDepositPriceButton.amount.text = s.toString()
            }
        })
    }

    private fun spinner() {
        val spinner = binding.includeDepositInput.selectedWallet
        val items = listOf("Choose wallet", "Binance", "OKX")

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        binding.includeDepositInput.selectedWallet.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedMethod = parent?.getItemAtPosition(position).toString()

                when (selectedMethod) {
                    "Choose Withdraw Method" -> {
                        binding.depositButton.isEnabled = false
                        Toast.makeText(this@DepositActivity,"Please select withdraw method",Toast.LENGTH_SHORT).show()
                    }
                    "Binance" -> {
                        depositMethod = "Binance ID"
                        binding.depositButton.isEnabled = true
                        // binding.myCashTv.visibility = View.VISIBLE
                    } "OKX" -> {
                    depositMethod = "OKX"
                    binding.depositButton.isEnabled = true
                    //  binding.myCashTv.visibility = View.VISIBLE
                }
                    // Add more cases if needed for other withdrawal methods
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle case when nothing is selected (if needed)
            }
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, RESULT_LOAD_IMAGE)

    }



    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == RESULT_LOAD_IMAGE && data != null) {
            binding.tvImageName.text = data.data.toString()
            binding.tvImageName.visibility = View.VISIBLE
            selectedImage = data.data!!
        }
        else{
            showMessage("No Image Selected")
        }
    }
    companion object {
        const val RESULT_LOAD_IMAGE = 1000
        const val PERMISSION_CODE_READ = 1001
        const val PERMISSION_CODE_WRITE = 1002
    }

    private fun copyClipBoard(textView: TextView) {

        val textToCopy = textView.text.split("\n")[0]
        val clipboardManager = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", textToCopy)
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_LONG).show()
    }

    private fun deposit() {
        val selectedImageUri = selectedImage
        if (selectedImageUri.toString() == "") {
            // Handle the case where no image is selected
            showMessage("No image selected")
        }else{
            val progressDialog = ProgressDialog(this)
            progressDialog.setTitle("Uploading...")
            progressDialog.setMessage("Uploading your image..")
            progressDialog.show()

            val ref : StorageReference = FirebaseStorage.getInstance().reference
                .child(UUID.randomUUID().toString())
            ref.putFile(selectedImage).addOnSuccessListener { taskSnapshot ->
                progressDialog.dismiss()
                taskSnapshot.storage.downloadUrl.addOnSuccessListener {
                    val url = it.toString()
                    updateImageToDatabase(url)
                }


            }.addOnFailureListener{
                progressDialog.dismiss()
                Toast.makeText(this, "Fail to Upload Image..", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun updateImageToDatabase(url: String) {

        val email = preferenceManager.getString(Constants.KEY_EMAIL)
        val id = firestore.collection(Constants.COLLECTION_DEPOSIT).document().id
        val map = hashMapOf<String,Any>(
            Constants.KEY_URL to url,
            Constants.KEY_BINANCE_ID to binding.includeDepositInput.binanceID.text.toString(),
            Constants.KEY_USERUID to preferenceManager.getString(Constants.KEY_USERUID),
            Constants.KEY_USERNAME to preferenceManager.getString(Constants.KEY_USERNAME),
            Constants.KEY_FULLNAME to preferenceManager.getString(Constants.KEY_FULLNAME),
            Constants.KEY_ID to id,
            Constants.KEY_AMOUNT to binding.includeDepositPriceButton.amount.text.toString(),
            Constants.KEY_STATUS to "pending",
            Constants.KEY_EMAIL to email,
            Constants.KEY_DEPOSIT_METHOD to depositMethod,
            Constants.KEY_DATE to currentDateTime.getCurrentDate().toString(),
            Constants.KEY_TIME to currentDateTime.getTimeWithAmPm().toString(),
            Constants.KEY_TIMESTAMP to currentDateTime.getTimeMiles().toString(),
        )
        firestore.collection(Constants.COLLECTION_DEPOSIT).document(id)
            .set(map).addOnCompleteListener{
                if (it.isSuccessful){
                    binding.tvImageName.visibility = View.INVISIBLE
                    binding.includeDepositInput.binanceID.setText("")
                    showMessage("deposit request submitted")
                    customNotification.ShowNotification(R.drawable.logo,"New Deposit Request submitted")
                    customNotification.saveNotification(
                        preferenceManager.getString(Constants.KEY_USERUID),"New Deposit Request"
                    ,"you have successfully submitted request to drago trade")
                    val intent = Intent(this, DepositSlipActivity::class.java)
                    intent.putExtra(Constants.KEY_AMOUNT,binding.includeDepositPriceButton.amount.text.toString())
                    startActivity(intent)

                }
            }.addOnFailureListener{
                Toast.makeText(this, "something went wrong!!", Toast.LENGTH_SHORT)
                    .show()
            }
    }

//    private fun requestGalleryPermission() {
//        ActivityCompat.requestPermissions(
//            this,
//            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
//            PERMISSION_REQUEST_CODE
//        )
//    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission is granted
                    showMessage("Permission granted, you can access the gallery")
                } else {
                    // Permission denied
                    showMessage("Permission denied, you cannot access the gallery")
                }
                return
            }
        }
    }

    fun notification(){

        createNotificationChannel()

        val builder = NotificationCompat.Builder(this,"new channel")
        builder.setSmallIcon(R.drawable.logo)
        .setContentTitle("New Deposit Request")
        .setContentText("you have successfully submitted request to drago trade")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)){
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notify(1,builder.build())
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel("new channel","First Channel",
                NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = "Test notification channel"

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}