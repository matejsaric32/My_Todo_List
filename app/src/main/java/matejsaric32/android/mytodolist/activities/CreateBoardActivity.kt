package matejsaric32.android.mytodolist.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import matejsaric32.android.mytodolist.R
import matejsaric32.android.mytodolist.databinding.ActivityCreateBoardBinding
import matejsaric32.android.mytodolist.firebase.FirestoreClass
import matejsaric32.android.mytodolist.models.Board
import matejsaric32.android.mytodolist.models.User
import matejsaric32.android.mytodolist.utils.Constants
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class CreateBoardActivity : BaseActivity() {

    private var binding: ActivityCreateBoardBinding? = null

    private var mSelectedImageFileUri: Uri? = null
    private var mProfileImageURL: String? = ""

    private lateinit var mUserDetails: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBoardBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setupActionBar()

        if(intent.hasExtra(Constants.USERS)){
            mUserDetails = intent.getParcelableExtra<User>(Constants.USERS) as User
            Toast.makeText(this, "Welcome ${mUserDetails.name}", Toast.LENGTH_SHORT).show()
        }

        binding?.btnCreateBoard?.setOnClickListener {
            if (mSelectedImageFileUri != null && binding?.etBoardName?.text.toString().isNotEmpty()) {
                uploadBoardImage()
            } else {
                Toast.makeText(this, "Please enter a board name", Toast.LENGTH_SHORT).show()
            }
        }

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


    fun boardCreatedSuccessfully(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun setupActionBar(){
        setSupportActionBar(binding?.toolbarCreateBoardActivity)

        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24)
            actionBar.title = resources.getString(R.string.create_board)
        }

        binding?.toolbarCreateBoardActivity?.setNavigationOnClickListener { onBackPressed() }

    }

    private fun getFileExtension(uri: Uri): String? {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri))
    }

    private fun takePhotoFromCamera() {

        checkPermission(Manifest.permission.CAMERA, Constants.CAMERA_PERMISSION_CODE)
        checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, Constants.STORAGE_PERMISSION_CODE)

        if (isPermissionGrantedForCamera()) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, Constants.CAMERA_PERMISSION_CODE)
        } else {
            Toast.makeText(this, "Permission denied2222", Toast.LENGTH_SHORT).show()
        }

    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constants.CAMERA_PERMISSION_CODE) {
                val thumbnail: Bitmap = data!!.extras!!.get("data") as Bitmap
                lifecycleScope.launch {
                    mSelectedImageFileUri = saveImage(thumbnail)
                    binding?.sivBoardImageCreateBoard?.setImageURI(mSelectedImageFileUri)
                }

//               saveImage(thumbnail)
                Log.e("Saved Image : ", "Path :: ${mSelectedImageFileUri.toString()}")
                Toast.makeText(this@CreateBoardActivity, "Image Saved!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun saveImage(bitmap: Bitmap) : Uri{
        var result : Uri? = null
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
                    result = f.toUri()

                    runOnUiThread{
                        if (result.toString().isNotEmpty()){
                            Toast.makeText(this@CreateBoardActivity,
                                "Image saved to gallery.",
                                Toast.LENGTH_SHORT).show()
                        }else{
                            Toast.makeText(this@CreateBoardActivity,
                                "Something went wrong.",
                                Toast.LENGTH_SHORT).show()
                        }
                    }

                }catch (e: Exception){

                    e.printStackTrace()
                }
            }
        }
        return result!!
    }

    private fun isPermissionGrantedForCamera(): Boolean {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
    }

    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this@CreateBoardActivity, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this@CreateBoardActivity, arrayOf(permission), Constants.CAMERA_PERMISSION_CODE)
        } else {
            Toast.makeText(this@CreateBoardActivity, "Permission already granted", Toast.LENGTH_SHORT).show()
        }
    }

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

    private fun choosePhotoFromGallary() {
        val galleryIntent = Intent(Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher.launch(galleryIntent)
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            mSelectedImageFileUri = result.data?.data
            Log.e("Selected Image : ", "Path :: ${mSelectedImageFileUri.toString()}")
            binding?.sivBoardImageCreateBoard?.setImageURI(mSelectedImageFileUri)
        }
    }


}