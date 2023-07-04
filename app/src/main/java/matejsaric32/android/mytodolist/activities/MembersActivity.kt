package matejsaric32.android.mytodolist.activities

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import matejsaric32.android.mytodolist.R
import matejsaric32.android.mytodolist.adapter.MemberAdapter
import matejsaric32.android.mytodolist.databinding.ActivityMembersBinding
import matejsaric32.android.mytodolist.databinding.DialogAddMembersBinding
import matejsaric32.android.mytodolist.firebase.FirestoreClass
import matejsaric32.android.mytodolist.firebase.NotificationData
import matejsaric32.android.mytodolist.firebase.NotificationUtils
import matejsaric32.android.mytodolist.firebase.PushNotification
import matejsaric32.android.mytodolist.models.Board
import matejsaric32.android.mytodolist.models.User
import matejsaric32.android.mytodolist.utils.Constants

/**
 * MembersActivity is class that controls activity_members.xml
 * Main task of this activity is to add members (other users) to specified board
 * Inherits properties form BaseActivity
 */

class MembersActivity : BaseActivity() {

    private var binding: ActivityMembersBinding? = null
    private var mBoardDetails: Board? = null

    private var mAssignedMembersList: ArrayList<User> = ArrayList()

    private lateinit var mAdapter: MemberAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMembersBinding.inflate(layoutInflater)
        setContentView(binding?.root)


        getDataFromIntent() /** Getting data from intent */
        setUpActionBar() /** Setting up action bar */

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getMembersFormBoardsList(this, mBoardDetails!!.assignedTo)
    }

    /**
     * Getting data from intent.
     *  @see onCreate
     */

    private fun getDataFromIntent() {
        if (intent.hasExtra(Constants.MEMBERS)) {
            mBoardDetails = intent.getParcelableExtra<Board>(Constants.MEMBERS) as Board
            Log.i("Board Name", mBoardDetails!!.name!!)
        } else{
            Log.e("BoardName", "No board name")
        }
    }

    /**
     * Function called when their has been a error in getting members list
     * @see FirestoreClass.getMembersFormBoardsList
     */

    fun failureMembersList(){
        hideProgressDialog()
        Toast.makeText(this, "Unable to get members list!!!", Toast.LENGTH_LONG)
    }

    /**
     * Function called when their has been a error adding member to list
     * @see FirestoreClass.assignMemberToBoard
     */

    fun memberAddedFaliure(){
        hideProgressDialog()
        Toast.makeText(this, "Unable to add new member to list!!!", Toast.LENGTH_LONG)
    }

    /**
     * Function called when there has been new member added successfully and to notify that change has been made to board
     * @see FirestoreClass.assignMemberToBoard
     */

    fun memberAdded(user: User) {
        hideProgressDialog()

        PushNotification(
            NotificationData("New Member", "${mBoardDetails?.name} has added you to board"),
            user.fcmToken!!
        ).also {
            NotificationUtils().sendNotificationToUser(it)
        }

        mAssignedMembersList.add(user)
        mAdapter.notifyDataSetChanged()
        mBoardDetails?.assignedTo?.add(user.id!!)
        setResult(Activity.RESULT_OK, Intent().putExtra(Constants.BOARD_ID, mBoardDetails))
        Toast.makeText(this, "Member added successfully", Toast.LENGTH_SHORT).show()
    }


    /**
     * Function that's called when we successfully added new member to list in firestore, function add new member to list
     * and calls function to assigned them to board
     * @see FirestoreClass.getMemberDetails
     */

    fun userDetails(user: User) {
        hideProgressDialog()
        mBoardDetails!!.assignedTo.add(user.id!!)
        FirestoreClass().assignMemberToBoard(this, mBoardDetails!!, user)
    }

    /**
     * A overridden function to create OptionsMenu
     */

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_member, menu)
        menu?.get(0)?.isVisible = if (FirestoreClass().isOnline(this)) true else false
        return super.onCreateOptionsMenu(menu)
    }

    /**
     * A overridden function to listen for witch item has been clicked and to execute corresponding function
     */

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_member -> {
                displayMemberSearchDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Function that's creates dialog where user need to enter email from another user to be added to list
     * @see onOptionsItemSelected
     * @see FirestoreClass.getMemberDetails
     */

    private fun displayMemberSearchDialog() {
        val dialog = Dialog(this)
        val binding = DialogAddMembersBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)
        binding.tvAdd.setOnClickListener {
            val email = binding.etEmailSearchMember.text.toString().trim()
            if (email.isNotEmpty()) {
                dialog.dismiss()
                showProgressDialog(resources.getString(R.string.please_wait))
                FirestoreClass().getMemberDetails(this, email)
                hideProgressDialog()
            } else {
                Toast.makeText(this, "Please enter a email", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    /**
     * Function to display all members from the board and display-them in recyclerview
     * @see setUpMembersList
     * @see MemberAdapter
     */

    fun setUpMembersList(membersList: ArrayList<User>) {
        hideProgressDialog()
        mAssignedMembersList = membersList

        if (membersList.size > 0) {

            binding?.rvMembersList?.layoutManager = LinearLayoutManager(this)
            binding?.rvMembersList?.setHasFixedSize(true)

            mAdapter = MemberAdapter(this, membersList)
            binding?.rvMembersList?.adapter = mAdapter

            mAdapter.setOnClickListener(object : MemberAdapter.OnClickListener{
                override fun onClick(position: Int, model: User) {
                    mBoardDetails!!.assignedTo.remove(model.id!!)
                    membersList.removeAt(position)
                    mBoardDetails!!.taskList.forEach { task ->
                        task.cards.forEach { card ->
                            card.assignedTo.remove(model.id!!)
                        }
                    }
                    mAdapter.notifyDataSetChanged()
                    FirestoreClass().addUpdateTaskList(this@MembersActivity, mBoardDetails!!)
                    setResult(Activity.RESULT_OK, Intent().putExtra(Constants.BOARD_ID, mBoardDetails))
                }
            })

        } else {
            binding?.rvMembersList?.visibility = View.GONE
        }
    }

    /**
     * A function for actionBar Setup.
     * @see onCreate
     */

    private fun setUpActionBar() {
        setSupportActionBar(binding?.toolbarMembersActivity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24_white)
            actionBar.title = "Members of:  ${mBoardDetails!!.name!!}"
        }
        binding?.toolbarMembersActivity?.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    fun updateSuccess() {
        setResult(Activity.RESULT_OK, Intent().putExtra(Constants.BOARD_ID, mBoardDetails))
    }
}