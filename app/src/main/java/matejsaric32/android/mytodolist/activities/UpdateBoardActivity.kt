package matejsaric32.android.mytodolist.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
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
import matejsaric32.android.mytodolist.databinding.ActivityUpdateBoardBinding
import matejsaric32.android.mytodolist.firebase.FirestoreClass
import matejsaric32.android.mytodolist.models.Board
import matejsaric32.android.mytodolist.utils.Constants

class UpdateBoardActivity : BaseActivity() {

    private var binding: ActivityUpdateBoardBinding? = null
    private var mBoardDetails: Board? = null

    private var mSelectedImageFileUri: Uri? = null
    private var mBoardImageURL: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateBoardBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        getDataFromIntent() /** Getting data from intent */
        setupActionBar() /** Setting up action bar */
        setUpForm() /** Setting up form  */

        /**
         * Listener when board profile picture is clicked to show dialog for user to choose
         * where he wants to provide the picture from gallery or camera
         */

        binding?.sivBoardImageUpdateBoard?.setOnClickListener {

            if(FirestoreClass().isOnline(this)) {
                val pictureDialog = AlertDialog.Builder(this)
                pictureDialog.setTitle("Select Action")
                val pictureDialogItems = arrayOf(
                    "Select photo from gallery",
                    "Capture photo from camera"
                )
                pictureDialog.setItems(pictureDialogItems) { dialog, which ->
                    when (which) {
                        0 -> choosePhotoFromGallary()
                        /** Calls the function to choose picture from gallery */
                        1 -> takePhotoFromCamera()
                        /** Calls the function to choose picture form camera */
                    }
                }
                pictureDialog.show()
            }else{
                showErrorSnackBar(resources.getString(R.string.offline_info))
            }
        }

        /**
         * Listener when button is clicked calls a function to update the board
         */

        binding?.btnUpdateBoard?.setOnClickListener {
            if (binding?.etBoardName?.text.toString().isNotEmpty()) {
                updateBoard()
            } else {
                Toast.makeText(this, "Please enter a board name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Function called when their has been a error updating board
     */

    fun updateBoardFailure(){
        hideProgressDialog()
        Toast.makeText(this, "Unable to update board!!!", Toast.LENGTH_LONG)
    }

    /**
     * A function that called when updating the Boards was successful and notify contract that change has been made
     * and puts new intent board to return
     * @see FirestoreClass.addUpdateTaskList
     */

    fun updateBoardSuccess(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK, Intent().putExtra(Constants.BOARD_ID, mBoardDetails))
        finish()
    }

    /**
     * A function to validate the form and to call a function in FirebaseClass to update the board
     * @see onCreate
     */

    private fun updateBoard(){

        var isFormValid = true

        if (binding?.etBoardName?.text!!.isEmpty()){
            isFormValid = false
        }

        if (isFormValid) {
            mBoardDetails?.image = mBoardImageURL
            mBoardDetails?.name = binding?.etBoardName?.text.toString()
            mBoardDetails?.taskList?.removeAt(mBoardDetails?.taskList?.size!! - 1)
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().addUpdateTaskList(this, mBoardDetails!!)
        }
    }

    /**
     * A function to upload image to Google storage
     * @see cameraContract
     * @see galleryContract
     */

    private fun uploadBoardImage(){
        showProgressDialog(resources.getString(R.string.please_wait))

        if (mSelectedImageFileUri != null) {
            val storageReference: StorageReference = FirebaseStorage.getInstance().reference.child(
                "BOARD_IMAGE" + mBoardDetails!!.name + "_" + System.currentTimeMillis() + "." + getFileExtension(mSelectedImageFileUri!!)
            )
            storageReference.putFile(mSelectedImageFileUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    Log.e("FirebaseImageURL", taskSnapshot.metadata!!.reference!!.downloadUrl.toString())
                    taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                        Log.e("Downloadable Image URL", uri.toString())
                        mBoardImageURL = uri.toString()
                    }
                }.addOnFailureListener { exception ->
                    Toast.makeText(
                        this@UpdateBoardActivity,
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

    /**
     * Camera contract to start Camera activity and if data is received changes displayed profile picture
     *  @see takePhotoFromCamera
     *  @see CameraActivity
     */

    var cameraContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data?.getStringExtra("image") == null) {
            mSelectedImageFileUri = Uri.parse(result.data?.getStringExtra("image"))
            Log.e("URI_camera", mSelectedImageFileUri.toString())
            uploadBoardImage()
            binding?.sivBoardImageUpdateBoard?.setImageURI(mSelectedImageFileUri)
        }else{
            Log.e("URI_camera", "No picture was taken")
        }
    }

    /**
     * Checks if precession is granted for camera and permission required.
     * @return - if permission is granted return true if its not return false
     * @see takePhotoFromCamera
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
        if (ContextCompat.checkSelfPermission(this@UpdateBoardActivity, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this@UpdateBoardActivity, arrayOf(permission), Constants.CAMERA_PERMISSION_CODE)
        } else {
            Toast.makeText(this@UpdateBoardActivity, "Permission already granted", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this@UpdateBoardActivity, "Camera Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@UpdateBoardActivity, "Camera Permission Denied", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == Constants.STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@UpdateBoardActivity, "Storage Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@UpdateBoardActivity, "Storage Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Function that launches contract for Gallery.
     * @see onCreate
     */

    private fun choosePhotoFromGallary() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryContract.launch(galleryIntent)
    }

    /**
     * Gallery contract for picking image.
     * @see choosePhotoFromGallary
     */

    private val galleryContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        mSelectedImageFileUri = result.data?.data
        binding?.sivBoardImageUpdateBoard?.setImageURI(mSelectedImageFileUri)
        if (result.resultCode == RESULT_OK) {
            mSelectedImageFileUri = result.data?.data
            Log.e("SelectedImage : ", "Path :: ${mSelectedImageFileUri.toString()}")
            uploadBoardImage()
            binding?.sivBoardImageUpdateBoard?.setImageURI(mSelectedImageFileUri)
        }
    }

    /**
     * Filling form from board that is being updated.
     * @see onCreate
     */

    private fun setUpForm() {

        if(mBoardDetails?.image!!.isNotEmpty()){
            Glide
                .with(this)
                .load(mBoardDetails?.image)
                .centerCrop()
                .placeholder(R.drawable.ic_baseline_account_circle_24)
                .into(binding?.sivBoardImageUpdateBoard!!)

        }
        binding?.etBoardName?.setText(mBoardDetails?.name)
    }

    /**
     * Getting data from intent.
     *  @see onCreate
     */

    private fun getDataFromIntent() {
        if (intent.hasExtra(Constants.BOARD_ID)) {
            mBoardDetails = intent.getParcelableExtra<Board>(Constants.BOARD_ID) as Board
            Log.i("BoardName", mBoardDetails!!.name!!)
            mBoardImageURL = mBoardDetails?.image
        }else{
            Log.e("BoardName", "No board name")
        }
    }

    /**
     * A function for actionBar Setup.
     * @see onCreate
     */

    private fun setupActionBar() {
        setSupportActionBar(binding?.toolbarUpdateBoardActivity)

        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = mBoardDetails?.name
            binding?.toolbarUpdateBoardActivity?.setNavigationIcon(R.drawable.ic_baseline_arrow_back_ios_24_white)
        }

        binding?.toolbarUpdateBoardActivity?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }

}