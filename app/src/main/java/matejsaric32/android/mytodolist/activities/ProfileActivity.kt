package matejsaric32.android.mytodolist.activities

import android.Manifest
import android.Manifest.permission.CAMERA
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
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
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.gms.common.data.BitmapTeleporter
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
import matejsaric32.android.mytodolist.utils.Constants.IMAGE_DIRECTORY
import java.io.*
import java.util.*

class ProfileActivity : BaseActivity() {

    private var binding: ActivityProfileBinding? = null

    private var mSelectedImageFileUri: Uri? = null
    private var mProfileImageURL: String? = ""
    private lateinit var mUserDetails: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setupActionBar()

        FirestoreClass().getUserData(this)


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

        binding?.btnUpdate?.setOnClickListener {
            if (mSelectedImageFileUri != null) {
                uploadUserProfileImage()
            } else {
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUserProfileData()
            }
        }
    }

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

    fun updateSuccess() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

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
                    binding?.sivPlaceImageProfile?.setImageURI(mSelectedImageFileUri)
                }

//               saveImage(thumbnail)
                Log.e("Saved Image : ", "Path :: ${mSelectedImageFileUri.toString()}")
                Toast.makeText(this@ProfileActivity, "Image Saved!", Toast.LENGTH_SHORT).show()
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
                            Toast.makeText(this@ProfileActivity,
                                "Image saved to gallery.",
                                Toast.LENGTH_SHORT).show()
                        }else{
                            Toast.makeText(this@ProfileActivity,
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
        if (ContextCompat.checkSelfPermission(this@ProfileActivity, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this@ProfileActivity, arrayOf(permission), Constants.CAMERA_PERMISSION_CODE)
        } else {
            Toast.makeText(this@ProfileActivity, "Permission already granted", Toast.LENGTH_SHORT).show()
        }
    }

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

    private fun choosePhotoFromGallary() {
        val galleryIntent = Intent(Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher.launch(galleryIntent)
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            mSelectedImageFileUri = result.data?.data
            Log.e("Selected Image : ", "Path :: ${mSelectedImageFileUri.toString()}")
            binding?.sivPlaceImageProfile?.setImageURI(mSelectedImageFileUri)
        }
    }

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

    private fun setupActionBar() {
        setSupportActionBar(binding?.toolbarProfileActivity)
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24)
        }

        binding?.toolbarProfileActivity?.setNavigationOnClickListener {
            onBackPressed()
        }
    }
}