package matejsaric32.android.mytodolist.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.Html
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import matejsaric32.android.mytodolist.R
import matejsaric32.android.mytodolist.adapter.AttachmentAdapter
import matejsaric32.android.mytodolist.adapter.CardMembersAdapter
import matejsaric32.android.mytodolist.adapter.CheckItemAdapter
import matejsaric32.android.mytodolist.databinding.ActivityCardBinding
import matejsaric32.android.mytodolist.dialogs.LabelColorListDialog
import matejsaric32.android.mytodolist.dialogs.MemberListDialog
import matejsaric32.android.mytodolist.firebase.FirestoreClass
import matejsaric32.android.mytodolist.firebase.NotificationData
import matejsaric32.android.mytodolist.firebase.NotificationUtils
import matejsaric32.android.mytodolist.firebase.PushNotification
import matejsaric32.android.mytodolist.models.*
import matejsaric32.android.mytodolist.utils.Constants
import java.text.SimpleDateFormat
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.ArrayList

class CardActivity : BaseActivity() {

    private var binding: ActivityCardBinding? = null
    private var mBoardDetails: Board? = null
    private var mCardPosition: Int? = null
    private var mTaskPosition: Int? = null
    private var mSelectedColor: String? = ""
    private lateinit var mMembersAssignedList: ArrayList<User>
    private lateinit var mMembersAssignedBoardList: ArrayList<User>

    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private var calendar = Calendar.getInstance()
    private var mSelectedDueDate: String? = null

    private var mSelectedMembersListBoard: ArrayList<SelectedMembers> = ArrayList()
    private var mSelectedMembersListFilter: ArrayList<SelectedMembers> = ArrayList()

    private var mAdapterMembersList: CardMembersAdapter? = null
    private var mAdapterCheckList: CheckItemAdapter? = null
    private var mAdapterAttachment: AttachmentAdapter? = null

    private var hasChanged = false

    private lateinit var mSelectedAttachment: String
    private lateinit var mSelectedAttachmentURL: String
    private var mSelectedAttachmentsURL: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        getDataForomIntent()

        setUpActionBar()

        setUpDisplay()

        binding?.btnUpdateCard?.setOnClickListener{
            updateCardDetails()
        }

        binding?.tvColorName?.setOnClickListener{
            colorListDialog()
        }

        binding?.ibEditCardNameStart?.setOnClickListener{
            binding?.etCardName?.focusable = View.FOCUSABLE
            binding?.ibEditCardNameStart?.visibility = View.GONE
            binding?.ibEditCardNameClose?.visibility = View.VISIBLE
        }

        binding?.ibEditCardNameClose?.setOnClickListener{
            binding?.etCardName?.focusable = View.NOT_FOCUSABLE
            binding?.ibEditCardNameStart?.visibility = View.VISIBLE
            binding?.ibEditCardNameClose?.visibility = View.GONE
        }


        mSelectedColor = mBoardDetails?.taskList?.get(mTaskPosition!!)?.cards?.get(mCardPosition!!)?.colorLabel
        if (mSelectedColor!!.isNotEmpty()) {
            setColor()
        }

        dateSetListener = DatePickerDialog.OnDateSetListener {
                view, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }

        binding?.tvDoDatePicker?.setOnClickListener{
            DatePickerDialog(
                this@CardActivity,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        setupSelectedMembersList()

        binding?.tvAttachment?.setOnClickListener{

            if (FirestoreClass().isOnline(this)){
                val pictureDialog = AlertDialog.Builder(this)
                pictureDialog.setTitle("Select Action")
                val pictureDialogItems = arrayOf(
                    "Select photo from gallery",
                    "Select photo from camera",
                    "Select file"
                )
                pictureDialog.setItems(pictureDialogItems) { dialog, which ->
                    when (which) {
                        0 -> choosePhotoFromGallary()
                        1 -> takePhotoFromCamera()
                        2 -> chooseFileFromMyFiles()
                    }
                }
                pictureDialog.show()
            } else {
                showErrorSnackBar("No internet connection, adding attachments is currently not possible. Connect to internet to restore this feature.")
            }
        }

        binding?.ivAddCheckListItem?.setOnClickListener{
            if (binding?.llAddCheckListItem?.visibility == View.GONE){
                binding?.llAddCheckListItem?.visibility = View.VISIBLE
            }else{
                binding?.llAddCheckListItem?.visibility = View.GONE
            }
        }

        binding?.ibCloseAddNewCheckListItem?.setOnClickListener {
            binding?.llAddCheckListItem?.visibility = View.GONE
        }

        binding?.ibFinishAddNewCheckListItem?.setOnClickListener {
            if (binding?.etAddNewCheckListItem?.text!!.isNotEmpty()){
                addNewCheckListItem()
            }else{
                Toast.makeText(this, "Please enter a check item", Toast.LENGTH_SHORT).show()
            }
        }

        setUpAttachmentRV()
    }

    private fun setUpAttachmentRV() {
        binding?.rvAttachments?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding?.rvAttachments?.setHasFixedSize(true)
        mAdapterAttachment = AttachmentAdapter(this, mBoardDetails!!.taskList[mTaskPosition!!].cards[mCardPosition!!].attachmentList)

        binding?.rvAttachments?.adapter = mAdapterAttachment

        mAdapterAttachment!!.setOnClickListener(object : AttachmentAdapter.OnClickListener{
            override fun onClick(position: Int, model: String) {
                mBoardDetails!!.taskList[mTaskPosition!!].cards[mCardPosition!!].attachmentList.removeAt(position)
                mAdapterAttachment!!.notifyDataSetChanged()
            }

        })
    }

    fun uploadAttachmentToStorage() {
        showProgressDialog(resources.getString(R.string.please_wait))
        if (!mSelectedAttachment.isNullOrEmpty()) {
            val storageReference: StorageReference = FirebaseStorage.getInstance().reference.child(
                "ATTACHMENT_IMAGE_" + System.currentTimeMillis() + "." + getFileExtension(Uri.parse(mSelectedAttachment!!))
            )
            storageReference.putFile(Uri.parse(mSelectedAttachment))
                .addOnSuccessListener { taskSnapshot ->
                    Log.e("FirebaseImageURL", taskSnapshot.metadata!!.reference!!.downloadUrl.toString())
                    taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                        Log.e("Downloadable Image URL", uri.toString())
                        mSelectedAttachmentURL = uri.toString()
                        mSelectedAttachmentsURL.add(mSelectedAttachmentURL)
                        mBoardDetails!!.taskList[mTaskPosition!!].cards[mCardPosition!!].attachmentList.add(mSelectedAttachmentURL)
                        mAdapterAttachment!!.notifyDataSetChanged()
                    }
                }.addOnFailureListener { exception ->
                    Toast.makeText(
                        this@CardActivity,
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

    var cameraContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "Image captured", Toast.LENGTH_SHORT).show()
            mSelectedAttachment = result.data?.getStringExtra("image")!!
            uploadAttachmentToStorage()
        }
    }

    private fun choosePhotoFromGallary() {
        val galleryIntent = Intent(Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryContract.launch(galleryIntent)
    }

    private fun chooseFileFromMyFiles() {
        var fileIntent = Intent(Intent.ACTION_GET_CONTENT)
//        fileIntent.addCategory(Intent.CATEGORY_OPENABLE)
        fileIntent.setType("*/*")
        fileContract.launch(fileIntent)
    }

    private val fileContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result ->
        if (result.resultCode == RESULT_OK) {
            mSelectedAttachment = result.data?.data.toString()
            Log.e("Selected Image : ", "Path :: ${mSelectedAttachment.toString()}")
            uploadAttachmentToStorage()
        }
    }

    private val galleryContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result ->
        if (result.resultCode == RESULT_OK) {
            mSelectedAttachment = result.data?.data.toString()
            Log.e("Selected Image : ", "Path :: ${mSelectedAttachment.toString()}")
            uploadAttachmentToStorage()
        }
    }

    fun updateCheckItemStatus(position: Int, isChecked: Boolean){
        mBoardDetails?.taskList?.get(mTaskPosition!!)?.cards?.get(mCardPosition!!)?.checkList?.get(position)?.apply {
            this.isChecked = isChecked
        }
        setResult(Activity.RESULT_OK, Intent().putExtra(Constants.BOARD_ID, mBoardDetails))
        mAdapterCheckList?.notifyItemChanged(position)
        hasChanged = true
    }

    fun updateCheckItem(position: Int, checkItem: String){
        mBoardDetails?.taskList?.get(mTaskPosition!!)?.cards?.get(mCardPosition!!)?.checkList?.get(position)?.apply {
            this.name = checkItem
        }
        mAdapterCheckList?.notifyItemChanged(position)
        setResult(Activity.RESULT_OK, Intent().putExtra(Constants.BOARD_ID, mBoardDetails))
        Log.d("Debugy", "Check item: ${checkItem.toString()}")
        hasChanged = true
    }

    private fun addNewCheckListItem(){
        val checkItem = CheckItem(
            binding?.etAddNewCheckListItem?.text.toString(),
            false
        )

        mBoardDetails?.taskList?.get(mTaskPosition!!)?.cards?.get(mCardPosition!!)?.checkList?.add(checkItem)
//        mAdapterCheckList?.notifyItemInserted(
//            mBoardDetails?.taskList?.get(mTaskPosition!!)?.cards?.get(mCardPosition!!)?.checkList?.size!! - 1)
        Log.d("Debugy", "Check item: ${checkItem.toString()}")

        binding?.etAddNewCheckListItem?.text = Editable.Factory.getInstance().newEditable("")
        binding?.llAddCheckListItem?.visibility = View.GONE

        mAdapterCheckList?.notifyDataSetChanged()
    }

    private fun updateDateInView() {
        val myFormat = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        mSelectedDueDate = sdf.format(calendar.time).toString()
        binding?.tvDoDatePicker?.setText(sdf.format(calendar.time).toString())
    }

    /**
     * Setting up recycler view for member assigned to
     */

    private fun setupSelectedMembersList(){
        val cardAssignedMembersList = mBoardDetails?.taskList?.
        get(mTaskPosition!!)?.cards?.get(mCardPosition!!)?.assignedTo

        for (user in mMembersAssignedList) {
            if (cardAssignedMembersList!!.contains(user.id)) {
                mSelectedMembersListBoard.add(SelectedMembers(user.id, user.image, user.name, true))
            } else {
                mSelectedMembersListBoard.add(SelectedMembers(user.id, user.image, user.name, false))
            }
        }

        if (mSelectedMembersListBoard.size > 0) {

            binding?.rvMembers?.visibility = View.VISIBLE

            binding?.rvMembers?.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

            mSelectedMembersListFilter = mSelectedMembersListBoard.filter { member ->
                member.isSelected
            } as ArrayList<SelectedMembers>

            mAdapterMembersList = CardMembersAdapter(this, mSelectedMembersListFilter)
            binding?.rvMembers?.adapter = mAdapterMembersList

            mAdapterMembersList!!.setOnClickListener(object : CardMembersAdapter.OnClickListener{
                override fun onClick() {
                    Toast.makeText(this@CardActivity, "Clicked", Toast.LENGTH_SHORT).show()
                    membersListDialog()
                }
            })

        } else {
            binding?.rvMembers?.visibility = View.GONE
        }
    }

    fun membersListDialog(){
        val listDialog = object : MemberListDialog(
            this,
            mSelectedMembersListBoard
        ){
            override fun onItemSelected(user: SelectedMembers, action: String) {

                if (action == Constants.SELECT) {
                    if (mSelectedMembersListBoard.contains(user)) {
                        mSelectedMembersListBoard.get(mSelectedMembersListBoard.indexOf(user)).isSelected = true
                        mSelectedMembersListFilter.add(user)
                        mAdapterMembersList?.notifyItemInserted(mSelectedMembersListFilter.size)
                        binding?.rvMembers?.adapter = mAdapterMembersList
                        Toast.makeText(this@CardActivity, "Member added ${user.name}", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    user.isSelected = false
                    mAdapterMembersList?.notifyItemRemoved(mSelectedMembersListFilter.indexOf(user))
                    mSelectedMembersListFilter.remove(user)
                }
            }
        }

        listDialog.show()
    }

    private fun colorListDialog(){
        val colorsList: ArrayList<String> = colorsList()
        val listDialog = object : LabelColorListDialog(
            this,
            colorsList,
            mSelectedColor!!
        ){
            override fun onItemSelected(color: String) {
                mSelectedColor = color
                Toast.makeText(this@CardActivity, "Selected color: $mSelectedColor", Toast.LENGTH_SHORT).show()

                setColor()
            }
        }
        listDialog.show()
    }

    private fun setColor(){
        binding?.tvColorName?.text = ""
        binding?.tvColorName?.setBackgroundColor(Color.parseColor(mSelectedColor))
    }

    private fun colorsList(): ArrayList<String>{
        val colorsList: ArrayList<String> = ArrayList()
        colorsList.add("#0069d9")
        colorsList.add("#5a6268")
        colorsList.add("#218838")
        colorsList.add("#c82333")
        colorsList.add("#e0a800")
        colorsList.add("#23272b")
        return colorsList
    }

    fun updateCardDetails(){
        val card = Card(
            binding?.etCardName?.text.toString(),
            mBoardDetails?.taskList!![mTaskPosition!!].cards[mCardPosition!!].createdBy,
            mSelectedMembersListFilter!!.map { it.id } as ArrayList<String>,
            mSelectedDueDate!!,
            mSelectedColor!!, mBoardDetails?.taskList!![mTaskPosition!!].cards[mCardPosition!!].checkList,
            mBoardDetails?.taskList!![mTaskPosition!!].cards[mCardPosition!!].attachmentList
        )

        mBoardDetails!!.taskList[mTaskPosition!!].cards[mCardPosition!!] = card
        showProgressDialog(resources.getString(R.string.please_wait))
        mBoardDetails!!.taskList.removeAt(mBoardDetails!!.taskList.size - 1)
        FirestoreClass().addUpdateTaskList(this, mBoardDetails!!, Constants.ACTIONS_TAKEN_UPDATE_CARD)
    }

    fun updatedeleteCardSuccess(actionType : String = "") {

        for (userFCM in mMembersAssignedList!!.stream().filter({ it.id != getCurrentUserID() }).collect(
            Collectors.toList())){

            val userThatMadeChange = mMembersAssignedList?.find { it.id == getCurrentUserID() }?.name
            val taskThatWasChanged = mBoardDetails!!.taskList[mTaskPosition!!].title
            val cardThatWasChanged = mBoardDetails!!.taskList[mTaskPosition!!].cards[mCardPosition!!].name

            when(actionType){
                Constants.ACTIONS_TAKEN_DELETE_CARD -> {
                    PushNotification(
                        NotificationData("Task deleted", "${userThatMadeChange} has deleted a card in ${taskThatWasChanged} "),
                        userFCM.fcmToken!!
                    ).also {
                        NotificationUtils().sendNotificationToUser(it)
                    }
                }
                Constants.ACTIONS_TAKEN_UPDATE_CARD -> {
                    PushNotification(
                        NotificationData("Task updated", "${userThatMadeChange} has updated a card in ${cardThatWasChanged} "),
                        userFCM.fcmToken!!
                    ).also {
                        NotificationUtils().sendNotificationToUser(it)
                    }
                }
            }

        }

        setResult(Activity.RESULT_OK, Intent().putExtra(Constants.BOARD_ID, mBoardDetails))
        hideProgressDialog()
        finish()
    }

    private fun deleteCard() {
        mBoardDetails!!.taskList[mTaskPosition!!].cards.removeAt(mCardPosition!!)
        mBoardDetails!!.taskList.removeAt(mBoardDetails!!.taskList.size - 1)
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this, mBoardDetails!!, Constants.ACTIONS_TAKEN_DELETE_CARD)
    }

    private fun alertDialogForDeleteList(title: String) {

        val builderMaterial = MaterialAlertDialogBuilder(this)
            .setTitle("Alert")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setMessage("Are you sure you want to delete $title.")
            .setPositiveButton(Html.fromHtml("<font color='#000000'>Yes</font>")) { dialog, which ->
                dialog.dismiss()
                if (this is CardActivity) {
                    this.deleteCard()
                }
            }
            .setNegativeButton(Html.fromHtml("<font color='#000000'>No</font>")){ dialog, which ->
                dialog.dismiss()}
            .create()

        builderMaterial.setCancelable(false)
        builderMaterial.show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_card_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete_card -> {
                alertDialogForDeleteList(mBoardDetails!!.taskList[mTaskPosition!!].cards[mCardPosition!!].name!!)
            }
            R.id.action_add_member_to_card -> {
                membersListDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setUpDisplay() {
        binding?.etCardName?.text = Editable.Factory.getInstance().newEditable(
            mBoardDetails!!.taskList[mTaskPosition!!].cards[mCardPosition!!].name)

        val labelColor = mBoardDetails!!.taskList[mTaskPosition!!].cards[mCardPosition!!].colorLabel
        val dueDate = mBoardDetails!!.taskList[mTaskPosition!!].cards[mCardPosition!!].dueDate
        mSelectedDueDate = dueDate

        if (labelColor!!.isNotEmpty()) {
            binding?.tvColorName?.text = labelColor
        } else {
            binding?.tvColorName?.text = "Pick Color"
        }

        if (dueDate!!.isNotEmpty()) {
            binding?.tvDoDatePicker?.text = dueDate
        } else {
            binding?.tvDoDatePicker?.text = "Pick Due Date"
        }

        binding?.rvChecklist?.layoutManager = LinearLayoutManager(this)
        binding?.rvChecklist?.setHasFixedSize(true)

        mAdapterCheckList = CheckItemAdapter(
            this, mBoardDetails!!.taskList[mTaskPosition!!].cards[mCardPosition!!].checkList)

        binding?.rvChecklist?.adapter = mAdapterCheckList

        mAdapterCheckList!!.setOnClickListener(object : CheckItemAdapter.OnClickListener {
            override fun onClick(position: Int, model: String) {
                Toast.makeText(this@CardActivity, "Clicked on item", Toast.LENGTH_SHORT).show()
            }

        })

    }

    private fun getDataForomIntent() {
        if (intent.hasExtra(Constants.BOARD_ID) && intent.hasExtra(Constants.CARD_LIST_POSITION)
            && intent.hasExtra(Constants.TASK_LIST_POSITION)  && intent.hasExtra(Constants.BOARD_MEMBERS_LIST)) {
            mBoardDetails = intent.getParcelableExtra<Board>(Constants.BOARD_ID) as Board
            mCardPosition = intent.getIntExtra(Constants.CARD_LIST_POSITION, -1)
            mTaskPosition = intent.getIntExtra(Constants.TASK_LIST_POSITION, -1)
            mMembersAssignedList = intent.getParcelableArrayListExtra<User>(Constants.BOARD_MEMBERS_LIST)!!
        }
    }

    private fun setUpActionBar() {
        setSupportActionBar(binding?.toolbarCardActivity)
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = mBoardDetails!!.taskList[mTaskPosition!!].cards[mCardPosition!!].name
        }
            binding?.toolbarCardActivity?.setNavigationOnClickListener {
                if (hasChanged) {
                    showProgressDialog(resources.getString(R.string.please_wait))
                    mBoardDetails!!.taskList.removeAt(mBoardDetails!!.taskList.size - 1)
                    FirestoreClass().addUpdateTaskList(this, mBoardDetails!!, Constants.ACTIONS_TAKEN_UPDATE_CARD)
                }
                onBackPressedDispatcher.onBackPressed()
            }
    }

    fun deleteCheckItem(position: Int) {
        mBoardDetails!!.taskList[mTaskPosition!!].cards[mCardPosition!!].checkList.removeAt(position)
        mAdapterCheckList!!.notifyDataSetChanged()

    }
}