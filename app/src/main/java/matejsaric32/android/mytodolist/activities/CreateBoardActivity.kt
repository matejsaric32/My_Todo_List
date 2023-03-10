package matejsaric32.android.mytodolist.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import matejsaric32.android.mytodolist.R
import matejsaric32.android.mytodolist.databinding.ActivityCreateBoardBinding
import matejsaric32.android.mytodolist.firebase.FirestoreClass
import matejsaric32.android.mytodolist.models.Board
import matejsaric32.android.mytodolist.models.User
import matejsaric32.android.mytodolist.utils.Constants

/**
 * CreateBoardActivity is class that controls activity_create_board.xml
 * Main task of this activity is for user to create a new board
 * Inherits properties form BaseActivity
 */

class CreateBoardActivity : BaseActivity() {

    private var binding: ActivityCreateBoardBinding? = null

    private var mSelectedImageFileUri: Uri? = null
    private var mProfileImageURL: String? = ""

    private lateinit var mUserDetails: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBoardBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setupActionBar() /** Setting up action bar */
        getDataFormIntent() /** Gets data from Intent */

        /**
         * Listener when button is clicked to call a function to create a new board
         */

        binding?.btnCreateBoard?.setOnClickListener {
            if (mSelectedImageFileUri != null && binding?.etBoardName?.text.toString().isNotEmpty()) {
                uploadBoardImage()
            } else {
                Toast.makeText(this, "Please enter a board name", Toast.LENGTH_SHORT).show()
            }
        }

        /**
         * Listener when board user pfogile picture is clicked to show dialog for user to choose
         * where he wants to provide the picture from gallery or camera
         */

        binding?.sivBoardImageCreateBoard?.setOnClickListener {
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
    }

    /**
     * Getting data from intent
     * @see onCreate
     */

    private fun getDataFormIntent() {
        if(intent.hasExtra(Constants.USERS)){
            mUserDetails = intent.getParcelableExtra<User>(Constants.USERS) as User
            Toast.makeText(this, "Welcome ${mUserDetails.name}", Toast.LENGTH_SHORT).show()
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
                "BOARD_IMAGE" + mUserDetails.name + "_" + System.currentTimeMillis() + "." + getFileExtension(mSelectedImageFileUri!!)
            )
            storageReference.putFile(mSelectedImageFileUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    Log.e("FirebaseImageURL", taskSnapshot.metadata!!.reference!!.downloadUrl.toString())
                    taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                        Log.e("Downloadable Image URL", uri.toString())
                        mProfileImageURL = uri.toString()
                        createBoard()
                    }
                }.addOnFailureListener { exception ->
                    Toast.makeText(
                        this@CreateBoardActivity,
                        exception.message,
                        Toast.LENGTH_LONG
                    ).show()
                    hideProgressDialog()
                }
        }
        hideProgressDialog()
    }

    /**
     * Function is called when board profile picture has been successfully uploaded
     * @see uploadBoardImage
     * @see boardCreatedSuccessfully
     */

    private fun createBoard(){
        val assignedUsersArrayList: ArrayList<String> = ArrayList()
        assignedUsersArrayList.add(getCurrentUserID())

        val board = Board(
            binding?.etBoardName?.text.toString(),
            mProfileImageURL,
            mUserDetails.name,
            assignedUsersArrayList
        )

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().createNewBoard(this, board)
    }

    /**
     * Function is called when board creation was successful and notify contract that change has been made
     * @see FirestoreClass.createNewBoard
     */

    fun boardCreatedSuccessfully(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    /**
     * Function is called when board creation was unsuccessful
     * @see FirestoreClass.createNewBoard
     */

    fun boardCreatedUnsuccessfully(){
        hideProgressDialog()
        finish()
    }

    /**
     * A function for actionBar Setup.
     * @see onCreate
     */

    private fun setupActionBar(){
        setSupportActionBar(binding?.toolbarCreateBoardActivity)

        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24)
            actionBar.title = resources.getString(R.string.create_board)
        }

        binding?.toolbarCreateBoardActivity?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

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
     * Camera contract to start Camera activity and if data is received changes displayed profile picture
     *  @see takePhotoFromCamera
     *  @see CameraActivity
     */

    var cameraContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "Image captured", Toast.LENGTH_SHORT).show()
            mSelectedImageFileUri = Uri.parse(result.data?.getStringExtra("image"))
            Log.e("URI", mSelectedImageFileUri.toString())
            binding?.sivBoardImageCreateBoard?.setImageURI(mSelectedImageFileUri)
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
        if (ContextCompat.checkSelfPermission(this@CreateBoardActivity, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this@CreateBoardActivity, arrayOf(permission), Constants.CAMERA_PERMISSION_CODE)
        } else {
            Toast.makeText(this@CreateBoardActivity, "Permission already granted", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this@CreateBoardActivity, "Camera Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@CreateBoardActivity, "Camera Permission Denied", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == Constants.STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@CreateBoardActivity, "Storage Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@CreateBoardActivity, "Storage Permission Denied", Toast.LENGTH_SHORT).show()
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
            Log.e("Selected Image : ", "Path :: ${mSelectedImageFileUri.toString()}")
            binding?.sivBoardImageCreateBoard?.setImageURI(mSelectedImageFileUri)
        }
    }

}