package matejsaric32.android.mytodolist.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import matejsaric32.android.mytodolist.R
import matejsaric32.android.mytodolist.adapter.TaskAdapter
import matejsaric32.android.mytodolist.databinding.ActivityTaskListBinding
import matejsaric32.android.mytodolist.firebase.FirestoreClass
import matejsaric32.android.mytodolist.models.Board
import matejsaric32.android.mytodolist.models.Card
import matejsaric32.android.mytodolist.models.Task
import matejsaric32.android.mytodolist.models.User
import matejsaric32.android.mytodolist.utils.Constants

class TaskListActivity : BaseActivity() {

    private var binding: ActivityTaskListBinding? = null
    private var mBoardDetails: Board? = null
    private var mAssignedMembersList: ArrayList<User>? = null
    private var mSelectedColor: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        getDataFromIntent() /** Getting data from intent*/
        setUpActionBar() /** Setting up action bar*/

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getBoardDetails(this, mBoardDetails!!.boardId!!) /** Return is in setUpBoardDetails()*/
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
     * #TODO prepraviti optimizacija callova na server
    */

//    override fun onResume() {
//        super.onResume()
//        FirestoreClass().getBoardDetails(this, mBoardDetails!!.boardId!!)
//    }


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

        Log.i("CardAdd", "added to ${mBoardDetails!!.taskList[position].cards.toString()}")
        mBoardDetails!!.taskList[position].cards.add(0,card)
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this, mBoardDetails!!)
    }

    /**
     * A function called from TaskAdapter to delete task from task list.
     */

    fun deleteTaskList(position: Int) {
        mBoardDetails!!.taskList.removeAt(position)
        mBoardDetails!!.taskList.removeAt(mBoardDetails!!.taskList.size - 1)
        showProgressDialog(resources.getString(R.string.please_wait))
        Log.i("TaskDelete", "position at ${position.toString()}")
        FirestoreClass().addUpdateTaskList(this, mBoardDetails!!)
    }

    /**
     * A function called from TaskAdapter to update task from task list.
     */

    fun updateTaskList(task: Task, position: Int, listName: String ) {
        val task = Task(listName, FirestoreClass().getCurrentUserID())
        mBoardDetails!!.taskList[position] = task
        mBoardDetails!!.taskList.removeAt(mBoardDetails!!.taskList.size - 1)
        showProgressDialog(resources.getString(R.string.please_wait))
        Log.i("TaskUpdate", "updated to ${mBoardDetails!!.taskList.size.toString()}")
        FirestoreClass().addUpdateTaskList(this, mBoardDetails!!)
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
        FirestoreClass().addUpdateTaskList(this, mBoardDetails!!)
        hideProgressDialog()
    }

    /**
     * A function called when firebase function for add/update was successful.
     */

    fun addUpdateTaskListSuccess() {
        hideProgressDialog()
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getBoardDetails(this, mBoardDetails!!.boardId!!)
        Toast.makeText(this, "Task List created successfully.", Toast.LENGTH_SHORT).show()
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
            supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24)
            supportActionBar!!.title = mBoardDetails!!.name
        }

        binding?.toolbarTaskListActivity?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}