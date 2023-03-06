package matejsaric32.android.mytodolist.activities

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import matejsaric32.android.mytodolist.R
import matejsaric32.android.mytodolist.adapter.CardMembersAdapter
import matejsaric32.android.mytodolist.databinding.ActivityCardBinding
import matejsaric32.android.mytodolist.dialogs.LabelColorListDialog
import matejsaric32.android.mytodolist.dialogs.MemberListDialog
import matejsaric32.android.mytodolist.firebase.FirestoreClass
import matejsaric32.android.mytodolist.models.Board
import matejsaric32.android.mytodolist.models.Card
import matejsaric32.android.mytodolist.models.SelectedMembers
import matejsaric32.android.mytodolist.models.User
import matejsaric32.android.mytodolist.utils.Constants
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CardActivity : BaseActivity() {

    private var binding: ActivityCardBinding? = null
    private var mBoardDetails: Board? = null
    private var mCardPosition: Int? = null
    private var mTaskPosition: Int? = null
    private var mSelectedColor: String? = ""
    private lateinit var mMembersAssignedList: ArrayList<User>

    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private var calendar = Calendar.getInstance()
    private var mSelectedDueDate: String? = null


    private var mAdapter: CardMembersAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardBinding.inflate(layoutInflater)
        setContentView(binding?.root)


        getDataForomIntent()


        setUpActionBar()

        setUpDisplay()

        binding?.btnUpdateCard?.setOnClickListener{
            privateUpgradeCardDetails()
        }

        binding?.tvColorName?.setOnClickListener{
            colorListDialog()
        }

        mSelectedColor = mBoardDetails?.taskList?.get(mTaskPosition!!)?.cards?.get(mCardPosition!!)?.colorLabel
        if (mSelectedColor!!.isNotEmpty()) {
            setColor()
        }

        binding?.tvSelectMembers?.setOnClickListener{
            membersListDialog()
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
    }

    private fun updateDateInView() {
        val myFormat = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        mSelectedDueDate = sdf.format(calendar.time).toString()
        binding?.tvDoDatePicker?.setText(sdf.format(calendar.time).toString())
    }

    private fun setupSelectedMembersList(){
        val cardAssignedMembersList = mBoardDetails?.taskList?.
        get(mTaskPosition!!)?.cards?.get(mCardPosition!!)?.assignedTo

        val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()

        if (cardAssignedMembersList!!.size > 0) {
            for (i in mMembersAssignedList.indices) {
                for (j in cardAssignedMembersList) {
                    if (mMembersAssignedList[i].id == j) {
                        val selectedMember = SelectedMembers(
                            mMembersAssignedList[i].id,
                            mMembersAssignedList[i].image,
                            mMembersAssignedList[i].name
                        )
                        selectedMembersList.add(selectedMember)
                    }
                }
            }
        } else {
            for (i in mMembersAssignedList.indices) {
                mMembersAssignedList[i].selected = false
            }
        }

        if (selectedMembersList.size > 0) {
//            selectedMembersList.add(SelectedMembers("", ""))

            binding?.rvMembers?.visibility = View.VISIBLE

            binding?.rvMembers?.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

            val mAdapter = CardMembersAdapter(this, selectedMembersList, true)
            binding?.rvMembers?.adapter = mAdapter

            mAdapter.setOnClickListener(object : CardMembersAdapter.OnClickListener{
                override fun onClick() {
                    membersListDialog()
                }
            })

        } else {
            binding?.tvSelectMembers?.visibility = View.VISIBLE
            binding?.rvMembers?.visibility = View.GONE
        }
    }

    private fun membersListDialog(){
        val cardAssignedMembersList = mBoardDetails?.taskList?.
        get(mTaskPosition!!)?.cards?.get(mCardPosition!!)?.assignedTo

        if (cardAssignedMembersList!!.size > 0) {
            for (i in mMembersAssignedList.indices) {
                for (j in cardAssignedMembersList) {
                    if (mMembersAssignedList[i].id == j) {
                        mMembersAssignedList[i].selected = true
                    }
                }
            }
        } else {
            for (i in mMembersAssignedList.indices) {
                mMembersAssignedList[i].selected = false
            }
        }

        val listDialog = object : MemberListDialog(
            this,
            mMembersAssignedList
        ){
            override fun onItemSelected(user: User, action: String) {
                if (action == Constants.SELECT) {
                    if (!mBoardDetails!!.taskList[mTaskPosition!!].cards[mCardPosition!!].assignedTo.contains(user.id)) {
                        mBoardDetails!!.taskList[mTaskPosition!!]
                            .cards[mCardPosition!!].assignedTo.add(user.id!!)
                        Toast.makeText(this@CardActivity, "Selected user: ${ mBoardDetails!!.taskList[mTaskPosition!!]
                            .cards[mCardPosition!!].assignedTo.size}", Toast.LENGTH_SHORT).show()
                        showProgressDialog(resources.getString(R.string.please_wait))
                        FirestoreClass().addUpdateTaskList(this@CardActivity, mBoardDetails!!)
                        setupSelectedMembersList()
                    }
                } else {
                    mBoardDetails!!.taskList[mTaskPosition!!].cards[mCardPosition!!].assignedTo.remove(user.id!!)
                    Toast.makeText(this@CardActivity, "Selected user: ${ mBoardDetails!!.taskList[mTaskPosition!!]
                        .cards[mCardPosition!!].assignedTo.size}", Toast.LENGTH_SHORT).show()
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

    fun privateUpgradeCardDetails(){
        val card = Card(
            binding?.etNameCardDetails?.text.toString(),
            mBoardDetails?.taskList!![mTaskPosition!!].cards[mCardPosition!!].createdBy,
            mBoardDetails?.taskList!![mTaskPosition!!].cards[mCardPosition!!].assignedTo,
            mSelectedDueDate!!,
            mSelectedColor!!
        )

        mBoardDetails!!.taskList[mTaskPosition!!].cards[mCardPosition!!] = card
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this, mBoardDetails!!)
    }

    fun updatedeleteCardSuccess() {
        hideProgressDialog()
        finish()
    }

    private fun deleteCard() {
        showProgressDialog(resources.getString(R.string.please_wait))
        mBoardDetails!!.taskList[mTaskPosition!!].cards.removeAt(mCardPosition!!)
        FirestoreClass().addUpdateTaskList(this, mBoardDetails!!)
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
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setUpDisplay() {
        binding?.etNameCardDetails?.text = Editable.Factory.getInstance().newEditable(
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
            supportActionBar?.title = mBoardDetails!!.name
        }
        binding?.toolbarCardActivity?.setNavigationOnClickListener { onBackPressed() }
    }
}