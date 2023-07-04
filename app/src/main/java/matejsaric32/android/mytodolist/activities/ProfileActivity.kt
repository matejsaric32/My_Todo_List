package matejsaric32.android.mytodolist.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import matejsaric32.android.mytodolist.R
import matejsaric32.android.mytodolist.databinding.ActivityProfileBinding
import matejsaric32.android.mytodolist.firebase.FirestoreClass
import matejsaric32.android.mytodolist.models.User
import matejsaric32.android.mytodolist.utils.Constants
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*

/**
 * ProfileActivity is class that controls activity_profile
 * Main task of this activity is for user to edit his profile
 * change his profile activity username and phone number
 * Inherits properties form BaseActivity
 */

class ProfileActivity : BaseActivity() {

    private var binding: ActivityProfileBinding? = null

    private var mSelectedImageFileUri: Uri? = null
    private var mProfileImageURL: String? = ""
    private lateinit var mUserDetails: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setupActionBar() /** Setting up action bar */
        FirestoreClass().getUserData(this) /** Gets user data from FirestoreClass*/

        /**
         * Listener when user profile picture is clicked to show dialog for user to choose
         * where he wants to provide the picture from gallery or camera
         */

        binding?.sivPlaceImageProfile?.setOnClickListener {
            if (FirestoreClass().isOnline(this)) {
                val pictureDialog = AlertDialog.Builder(this)
                pictureDialog.setTitle("Select Action")
                val pictureDialogItems = arrayOf(
                    "Select photo from gallery",
                    "Capture photo from camera"
                )
                pictureDialog.setItems(pictureDialogItems) { dialog, which ->
                    when (which) {
                        0 -> choosePhotoFromGallery()
                        1 -> takePhotoFromCamera()
                    }
                }
                pictureDialog.show()
            }else{
                showErrorSnackBar(resources.getString(R.string.offline_info))
            }
        }

        /**
         * Listener when button is clicked calls a function to update the user info
         */

        binding?.btnUpdate?.setOnClickListener {
            uploadUserProfileImage()
            if (FirestoreClass().isOnline(this)) {
                if (mSelectedImageFileUri != null) {
                    showProgressDialog(resources.getString(R.string.please_wait))
                    uploadUserProfileImage()
                } else {
                    showProgressDialog(resources.getString(R.string.please_wait))
                    updateUserProfileData()
                }
            }else{
                showErrorSnackBar(resources.getString(R.string.offline_info))
            }
        }

        if (!FirestoreClass().isOnline(this)){
            binding?.etPhone?.focusable = View.NOT_FOCUSABLE
            binding?.etName?.focusable = View.NOT_FOCUSABLE
            binding?.etEmail?.focusable = View.NOT_FOCUSABLE
        }
    }


    /**
     * Function validates form inputs and calls Firestorefunction to update the user
     * @see onCreate
     * @see FirestoreClass.updateUserData
     */

    private fun updateUserProfileData(){
        val userHashMap = HashMap<String, Any>()
        var isChange = false
        if (mProfileImageURL!!.isNotEmpty()) {
            userHashMap[Constants.IMAGE] = mProfileImageURL!!
            isChange = true
        }
        if(binding?.etName?.text.toString() != mUserDetails.name){
            userHashMap[Constants.NAME] = binding?.etName?.text.toString()
            isChange = true
        }
        if (binding?.etPhone?.text!!.isNotEmpty() )  {
            if(binding?.etPhone?.text.toString() != mUserDetails.mobile.toString()){
                userHashMap[Constants.MOBILE] = binding?.etPhone?.text.toString().toLong()
                isChange = true
            }
        }


        if (isChange) {
            FirestoreClass().updateUserData(this, userHashMap)
        } else {
            hideProgressDialog()
        }
    }

    /**
     * Function that is called when updating user was successful
     * @see FirestoreClass.updateUserData
     */

    fun updateFailure(){
        hideProgressDialog()
        showErrorSnackBar("There was a problem updating user profile")
    }

    /**
     * Function that is called when updating user was successful
     * @see FirestoreClass.updateUserData
     */

    fun updateSuccess() {
        hideProgressDialog()
        mUserDetails = User(
            FirestoreClass().getCurrentUserID(),
            binding?.etName?.text.toString(),
            binding?.etPhone?.text.toString(),
            mProfileImageURL!!
        )
        setResult(Activity.RESULT_OK, Intent().putExtra(Constants.USERS, mUserDetails))
        finish()
    }

    /**
     * A function to upload image to Google storage, delete existing one and delete image from storage
     * @see cameraContract
     * @see galleryContract
     */

    private fun uploadUserProfileImage() {
        showProgressDialog(resources.getString(R.string.please_wait))
        val tmp = mSelectedImageFileUri
        if (mSelectedImageFileUri != null) {
            val storageReference: StorageReference = FirebaseStorage.getInstance().reference.child(
                "USER_IMAGE" + System.currentTimeMillis() + "." + getFileExtension(mSelectedImageFileUri!!)
            )
            storageReference.putFile(mSelectedImageFileUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    Log.e("Firebase Image URL", taskSnapshot.metadata!!.reference!!.downloadUrl.toString())
                    taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                        Log.e("Downloadable Image URL", uri.toString())
                        mProfileImageURL = uri.toString()
                        updateUserProfileData()
                    }
                }.addOnFailureListener { exception ->
                Toast.makeText(
                    this@ProfileActivity,
                    exception.message,
                    Toast.LENGTH_LONG
                ).show()
                hideProgressDialog()
            }
            val deleteFileByURl = storageReference.child(tmp.toString())

            deleteFileByURl.delete().addOnSuccessListener {
                Log.i("DeleteURL", "Success")
            }.addOnFailureListener {
                Log.e("DeleteURL", "Failed")
            }
        }

        hideProgressDialog()
    }

    /**
     * Function to get URI's file extension
     * @param uri: Uri - uri from file that user has selected to be a new profile of this bord
     * @see uploadBoardImage
     */

    private fun getFileExtension(uri: Uri): String? {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri))
    }

    /**
     * Function to check privileges and start camera
     * @see onCreate
     */

    private fun takePhotoFromCamera() {

        if (wasCameraPermissionWasGiven()) {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, Constants.CAMERA_PERMISSION_CODE)
        } else {
            Toast.makeText(this, "Camera permission are missing", Toast.LENGTH_SHORT).show()

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_MEDIA_IMAGES
                    )
                )
            } else {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                )
            }
        }
    }

    private fun wasCameraPermissionWasGiven() : Boolean {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                return true
            }
            return false
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        return false

    }

    val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        Log.d("TAG123", "onCreate: requestPermissionLauncher ${it}")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.CAMERA_PERMISSION_CODE){
            Log.d("TAG123", "onActivityResult: ${data?.data}")
            mProfileImageURL = data?.extras?.get("data").toString()
            val images: Bitmap = data?.extras?.get("data") as Bitmap

            lifecycleScope.launch {
                saveImageToGallery(images)
            }

//            binding?.sivPlaceImageProfile?.setImageBitmap(images)
        }
    }


    private suspend fun saveImageToGallery(bitmap: Bitmap) {
        var result = ""
        withContext(Dispatchers.IO){
            if (bitmap != null){
                try {
                    val bytes = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)
                    Log.i("Image", "Image saved to gallery.")
                    val f = File(externalCacheDir?.absoluteFile.toString() + File.separator
                            + "DrawingApp_" + System.currentTimeMillis() / 1000 + ".png")

                    f.createNewFile()

                    val fo = FileOutputStream(f)
                    fo.write(bytes.toByteArray())
                    fo.close()
                    result = f.absolutePath

                    mProfileImageURL = Uri.fromFile(f).toString()

                    binding?.sivPlaceImageProfile?.setImageURI(Uri.fromFile(f))
                    Log.d("TAG123", "onActivityResult: ${Uri.fromFile(f).toString()}")


                }catch (e: Exception){
                    result = ""
                    e.printStackTrace()
                }
            }
        }
    }


    /**
     * Function that launches contract for Gallery.
     * @see onCreate
     */

    private fun choosePhotoFromGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryContract.launch(galleryIntent)
    }

    /**
     * Gallery contract for picking image.
     * @see choosePhotoFromGallery
     */

    private val galleryContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            mSelectedImageFileUri = result.data?.data
            Log.e("SelectedImage:", "Path :: ${mSelectedImageFileUri.toString()}")
            binding?.sivPlaceImageProfile?.setImageURI(mSelectedImageFileUri)
        }else{
            Log.e("CancelledGalleryImagePicker", "Cancelled")
        }
    }

    /**
     * Filling user data form that is being updated
     * @see FirestoreClass.getUserData
     */

    fun setUserData(user: User){

        mUserDetails = user

        if (mUserDetails.image!!.isNotEmpty()){
            Glide
                .with(this)
                .load(user.image)
                .centerCrop()
                .placeholder(R.drawable.ic_baseline_account_circle_24)
                .into(binding?.sivPlaceImageProfile!!)
        }

        binding?.etName?.text = Editable.Factory.getInstance().newEditable(user.name)
        binding?.etEmail?.text = Editable.Factory.getInstance().newEditable(user.email)

        if (user.mobile != 0L){
            binding?.etPhone?.text = Editable.Factory.getInstance().newEditable(user.mobile.toString())
        }else{
            binding?.etPhone?.text = Editable.Factory.getInstance().newEditable("")
        }
    }

    /**
     * A function for actionBar Setup.
     * @see onCreate
     */

    private fun setupActionBar() {
        setSupportActionBar(binding?.toolbarProfileActivity)
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24_white)
        }

        binding?.toolbarProfileActivity?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}