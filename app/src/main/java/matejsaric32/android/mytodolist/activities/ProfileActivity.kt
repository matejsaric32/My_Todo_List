package matejsaric32.android.mytodolist.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import matejsaric32.android.mytodolist.R
import matejsaric32.android.mytodolist.databinding.ActivityProfileBinding
import matejsaric32.android.mytodolist.firebase.FirestoreClass
import matejsaric32.android.mytodolist.models.User
import matejsaric32.android.mytodolist.utils.Constants
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

            val pictureDialog = AlertDialog.Builder(this)
            pictureDialog.setTitle("Select Action")
            val pictureDialogItems = arrayOf("Select photo from gallery",
                "Capture photo from camera")
            pictureDialog.setItems(pictureDialogItems) {
                    dialog, which ->
                when (which) {
                    0 -> choosePhotoFromGallary()
                    1 -> takePhotoFromCamera()
                }
            }
            pictureDialog.show()
        }

        /**
         * Listener when button is clicked calls a function to update the user info
         */

        binding?.btnUpdate?.setOnClickListener {
            Toast.makeText(this, "Update", Toast.LENGTH_SHORT).show()
            if (mSelectedImageFileUri != null) {
                showProgressDialog(resources.getString(R.string.please_wait))
                uploadUserProfileImage()
                updateUserProfileData()
            } else {
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUserProfileData()
            }
        }
    }

    /**
     * Camera contract to start Camera activity and if data is received changes displayed profile picture
     *  @see takePhotoFromCamera
     *  @see CameraActivity
     */

    var cameraContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "Image captured", Toast.LENGTH_LONG).show()
            mSelectedImageFileUri = Uri.parse(result.data?.getStringExtra("image"))
            Log.e("URI", mSelectedImageFileUri.toString())
            binding?.sivPlaceImageProfile?.setImageURI(mSelectedImageFileUri)
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
        if (mProfileImageURL!!.isNotEmpty() && mProfileImageURL != mUserDetails.image) {
            userHashMap[Constants.IMAGE] = mProfileImageURL!!
            isChange = true
        }
        if(binding?.etName?.text.toString() != mUserDetails.name){
            userHashMap[Constants.NAME] = binding?.etName?.text.toString()
            isChange = true
        }
        if(binding?.etPhone?.text.toString() != mUserDetails.mobile.toString()){
            userHashMap[Constants.MOBILE] = binding?.etPhone?.text.toString().toLong()
            isChange = true
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
        setResult(Activity.RESULT_OK)
        finish()
    }

    /**
     * A function to upload image to Google storage
     * @see cameraContract
     * @see galleryContract
     */

    private fun uploadUserProfileImage() {
        showProgressDialog(resources.getString(R.string.please_wait))
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

        checkPermission(Manifest.permission.CAMERA, Constants.CAMERA_PERMISSION_CODE)
        checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, Constants.STORAGE_PERMISSION_CODE)

        if (isPermissionGrantedForCamera()) {
            val intent = Intent(this, CameraActivity::class.java)
            cameraContract.launch(intent)
        } else {
            Toast.makeText(this, "Permission denied2222", Toast.LENGTH_SHORT).show()
        }

    }

    /**
     * Camera contract to start Camera activity
     *  @see takePhotoFromCamera
     *  @see CameraActivity
     */

    private fun isPermissionGrantedForCamera(): Boolean {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
    }

    /**
     * Function that checks if permission is granted if its not it requests it.
     * @param permission - permission type
     * @param requestCode - permission code to ensure the right permission
     * @see takePhotoFromCamera
     */

    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this@ProfileActivity, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this@ProfileActivity, arrayOf(permission), Constants.CAMERA_PERMISSION_CODE)
        } else {
            Toast.makeText(this@ProfileActivity, "Permission already granted", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Overridden function to request permission.
     * @param requestCode
     * @param premissions
     * @param grantResults
     * @see takePhotoFromCamera
     */

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@ProfileActivity, "Camera Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@ProfileActivity, "Camera Permission Denied", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == Constants.STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@ProfileActivity, "Storage Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@ProfileActivity, "Storage Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Function that launches contract for Gallery.
     * @see onCreate
     */

    private fun choosePhotoFromGallary() {
        val galleryIntent = Intent(Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryContract.launch(galleryIntent)
    }

    /**
     * Gallery contract for picking image.
     * @see choosePhotoFromGallary
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

        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_baseline_account_circle_24)
            .into(binding?.sivPlaceImageProfile!!)

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
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24)
        }

        binding?.toolbarProfileActivity?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}