package matejsaric32.android.mytodolist.activities

import android.app.Activity
import android.content.Intent
import android.graphics.PostProcessor
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import matejsaric32.android.mytodolist.R
import matejsaric32.android.mytodolist.adapter.TaskAdapter
import matejsaric32.android.mytodolist.databinding.ActivityTaskListBinding
import matejsaric32.android.mytodolist.firebase.FirestoreClass
import matejsaric32.android.mytodolist.firebase.NotificationData
import matejsaric32.android.mytodolist.firebase.NotificationUtils
import matejsaric32.android.mytodolist.firebase.PushNotification
import matejsaric32.android.mytodolist.models.Board
import matejsaric32.android.mytodolist.models.Card
import matejsaric32.android.mytodolist.models.Task
import matejsaric32.android.mytodolist.models.User
import matejsaric32.android.mytodolist.utils.Constants
import java.util.Collections
import java.util.stream.Collectors
import java.util.stream.Stream

class TaskListActivity : BaseActivity() {

    private var binding: ActivityTaskListBinding? = null
    private var mBoardDetails: Board? = null
    private var mAssignedMembersList: ArrayList<User>? = null
    private var mSelectedColor: String = ""

    private var hasChanged = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        getDataFromIntent() /** Getting data from intent*/
        setUpActionBar() /** Setting up action bar*/

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getBoardDetails(this, mBoardDetails!!.boardId!!) /** Return is in setUpBoardDetails()*/
    }

    fun swipeCardPlaces(cards : ArrayList<Card>, position: Int){
        mBoardDetails!!.taskList[position].cards = cards
        Log.d("swipeCardPlaces", mBoardDetails!!.taskList[position].cards.toString())
        hasChanged = true
    }

    /**
     * Function called when deleting board was successful
     */

    fun deleteBoardSucces(){
        setResult(Activity.RESULT_OK)
        finish()
    }

    /**
     * A function for getting data from Intent.
     */

    private fun getDataFromIntent() {
        if (intent.hasExtra(Constants.BOARD_ID)) {
            mBoardDetails = intent.getParcelableExtra<Board>(Constants.BOARD_ID) as Board
            Log.i("Task list board from intetn", mBoardDetails!!.name!!)
        }
    }

    /**
     * Function called from Firebase to get members of the board.
     */

    fun boardMembersDetails(list: ArrayList<User>){
        mAssignedMembersList = list
        hideProgressDialog()
    }

    /**
     * Functin called form task adapter when card is to start card is clicked
     */

    fun cardDetails(taskPosition: Int, cardPosition: Int) {
        val intent = Intent(this, CardActivity::class.java)
        intent.putExtra(Constants.BOARD_ID, mBoardDetails!!)
        intent.putExtra(Constants.CARD_LIST_POSITION, cardPosition)
        intent.putExtra(Constants.TASK_LIST_POSITION, taskPosition)
        intent.putExtra(Constants.BOARD_MEMBERS_LIST, mAssignedMembersList)
        cardDetailContract.launch(intent)
    }

    var cardDetailContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            mBoardDetails = result.data?.getParcelableExtra<Board>(Constants.BOARD_ID) as Board
            FirestoreClass().getBoardDetails(this, mBoardDetails!!.boardId!!)
        }
    }

    /**
     * A overridden function to create OptionsMenu
     * @param menu - menu that's used
     */

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members, menu)
        return super.onCreateOptionsMenu(menu)
    }

    /**
     * A overridden function to listen for witch item has been clicked and to execute corresponding function
     * @param item - view that was clicked
     */

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_member -> {
                val intent = Intent(this@TaskListActivity , MembersActivity::class.java)
                intent.putExtra(Constants.MEMBERS, mBoardDetails)
                updateBoardContract.launch(intent)
            }
            R.id.update_board -> {
                val intent = Intent(this@TaskListActivity , UpdateBoardActivity::class.java)
                intent.putExtra(Constants.BOARD_ID, mBoardDetails)
                updateBoardContract.launch(intent)

            }
            R.id.delete_board -> {
                alertDialogForDeleteBoard()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Alert dialog for deleting board
     * @see onOptionsItemSelected
     * @see FirestoreClass.deleteBoard
     */

    private fun alertDialogForDeleteBoard() {

        val builderMaterial = MaterialAlertDialogBuilder(this)
            .setTitle("Alert")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setMessage("Are you sure you want to delete ${mBoardDetails!!.name}.")
            .setPositiveButton(Html.fromHtml("<font color='#000000'>Yes</font>")) { dialog, which ->
                dialog.dismiss()
                if (this is TaskListActivity) {
                    FirestoreClass().deleteBoard(this, mBoardDetails!!)
                }
            }
            .setNegativeButton(Html.fromHtml("<font color='#000000'>No</font>")){ dialog, which ->
                dialog.dismiss()}
            .create()

        builderMaterial.setCancelable(false)
        builderMaterial.show()
    }

    /**
     * Contract for updating board activity
     */

    var updateBoardContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d("debugy","updateBoardContract")
            setResult(Activity.RESULT_OK)
            mBoardDetails = result.data?.getParcelableExtra<Board>(Constants.BOARD_ID) as Board
            setUpActionBar()
            FirestoreClass().getBoardDetails(this, mBoardDetails!!.boardId!!)
        }
    }

    /**
     * A function called from TaskAdapter to add new card task.
     */

    fun addCardToTaskList(position: Int, cardName : String) {
        val cardAssignedUserList : ArrayList<String> = ArrayList()
        cardAssignedUserList.add(FirestoreClass().getCurrentUserID())
        Toast.makeText(this, "${cardName}", Toast.LENGTH_SHORT).show()

        val card = Card(cardName,
            FirestoreClass().getCurrentUserID(), cardAssignedUserList)

        mBoardDetails!!.taskList.removeAt(mBoardDetails!!.taskList.size - 1)
//        Log.i("CardAdd", "added to ${mBoardDetails!!.taskList[position].cards.toString()}")
        mBoardDetails!!.taskList[position].cards.add(0, card)
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this, mBoardDetails!!, Constants.ACTIONS_TAKEN_INSERT_CARD)
    }

    /**
     * A function called from TaskAdapter to delete task from task list.
     */

    fun deleteTaskList(position: Int) {
        mBoardDetails!!.taskList.removeAt(position)
        mBoardDetails!!.taskList.removeAt(mBoardDetails!!.taskList.size - 1)
        showProgressDialog(resources.getString(R.string.please_wait))
        Log.i("TaskDelete", "position at ${position.toString()}")
        FirestoreClass().addUpdateTaskList(this, mBoardDetails!!, Constants.ACTIONS_TAKEN_DELETE)
    }

    /**
     * A function called from TaskAdapter to update task from task list.
     */

    fun updateTaskList(task: Task, position: Int, listName: String ) {
        val task = Task(listName, FirestoreClass().getCurrentUserID(), mBoardDetails!!.taskList[position].cards)
        mBoardDetails!!.taskList[position] = task
        mBoardDetails!!.taskList.removeAt(mBoardDetails!!.taskList.size - 1)
        showProgressDialog(resources.getString(R.string.please_wait))
        Log.i("TaskUpdate", "updated to ${mBoardDetails!!.taskList.size.toString()}")
        FirestoreClass().addUpdateTaskList(this, mBoardDetails!!, Constants.ACTIONS_TAKEN_UPDATE)
    }

    /**
     * A function called from TaskAdapter to create task from task list.
     */

    fun createTaskList(taskListName: String) {
        val task = Task(taskListName, FirestoreClass().getCurrentUserID())
        mBoardDetails!!.taskList.add(0, task)
        Toast.makeText(this, "${mBoardDetails!!.taskList.size.toString()}", Toast.LENGTH_SHORT).show()
        mBoardDetails!!.taskList.removeAt(mBoardDetails!!.taskList.size - 1)
        showProgressDialog(resources.getString(R.string.please_wait))
        Log.i("TaskCreate", "created to ${mBoardDetails!!.taskList.size.toString()}")
        FirestoreClass().addUpdateTaskList(this, mBoardDetails!!, Constants.ACTIONS_TAKEN_INSERT)
        hideProgressDialog()
    }

    /**
     * A function called when firebase function for add/update was successful.
     */

    fun addUpdateTaskListSuccess(isOffline : Boolean = false, actionType : String = "") {
        hideProgressDialog()
        for (user in mAssignedMembersList!!.stream().filter({ it.id != getCurrentUserID() }).collect(
            Collectors.toList())) {
            Log.d("debugy", "${user.fcmToken}")

            val userThatMadeChange = mAssignedMembersList?.find { it.id == getCurrentUserID() }?.name

            when(actionType){
                Constants.ACTIONS_TAKEN_UPDATE -> {
                    PushNotification(
                        NotificationData("Board was updated", "${userThatMadeChange} has updated ${mBoardDetails?.name} board."),
                        user.fcmToken!!
                    ).also {
                        NotificationUtils().sendNotificationToUser(it)
                    }
                }
                Constants.ACTIONS_TAKEN_INSERT -> {
                    PushNotification(
                        NotificationData("New task was created", "${userThatMadeChange} has created new task in ${mBoardDetails?.name} board."),
                        user.fcmToken!!
                    ).also {
                        NotificationUtils().sendNotificationToUser(it)
                    }
                }
                Constants.ACTIONS_TAKEN_DELETE -> {
                    PushNotification(
                        NotificationData("Task was deleted", "${userThatMadeChange} has deleted task in ${mBoardDetails?.name} board."),
                        user.fcmToken!!
                    ).also {
                        NotificationUtils().sendNotificationToUser(it)
                    }
                }
                Constants.ACTIONS_TAKEN_INSERT_CARD -> {
                    PushNotification(
                        NotificationData("New card was created", "${userThatMadeChange} has created new card in ${mBoardDetails?.name} board."),
                        user.fcmToken!!
                    ).also {
                        NotificationUtils().sendNotificationToUser(it)
                    }
                }
            }

        }

        showProgressDialog(resources.getString(R.string.please_wait))
        if (isOffline) {
            setUpBoardDetails(mBoardDetails!!)
        }else{
            FirestoreClass().getBoardDetails(this, mBoardDetails!!.boardId!!)
        }
        hideProgressDialog()
    }

    /**
     * Function called when fetching data from firebase was successful.
     */

    fun setUpBoardDetails(board: Board) {
        hideProgressDialog()

        board.taskList.add(Task(resources.getString(R.string.add_list)))
        mBoardDetails = board
        binding?.rvTaskList?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding?.rvTaskList?.setHasFixedSize(true)
        val adapter = TaskAdapter(this, board.taskList)
        binding?.rvTaskList?.adapter = adapter

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getMembersFormBoardsList(this, mBoardDetails!!.assignedTo) /** Function to get members from firebase */
    }

    /**
     * A function for actionBar Setup.
     */

    private fun setUpActionBar() {
       setSupportActionBar(binding?.toolbarTaskListActivity)

        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24_white)
            supportActionBar!!.title = mBoardDetails!!.name
        }

        binding?.toolbarTaskListActivity?.setNavigationOnClickListener {
            if (hasChanged){
                showProgressDialog(resources.getString(R.string.please_wait))
                FirestoreClass().addUpdateTaskList(this, mBoardDetails!!)
            }
            onBackPressedDispatcher.onBackPressed()
        }
    }
}