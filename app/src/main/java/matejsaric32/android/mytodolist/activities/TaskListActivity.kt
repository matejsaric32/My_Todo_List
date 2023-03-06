package matejsaric32.android.mytodolist.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
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
    companion object {
        const val MEMBERS_ADD_CODE: Int = 16
        const val CARD_DELETE_CODE: Int = 32
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        if (intent.hasExtra(Constants.BOARD_ID)) {
            mBoardDetails = intent.getParcelableExtra<Board>(Constants.BOARD_ID) as Board
            Log.e("Board Name", mBoardDetails!!.name!!)
        }

        setUpActionBar()

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getBoardDetails(this, mBoardDetails!!.boardId!!)

    }

    fun boardMembersDetails(list: ArrayList<User>){
        mAssignedMembersList = list
        hideProgressDialog()
//
////        val addTaskList = Task("resources.getString(R.string.add_list)")
////        mBoardDetails!!.taskList.add(addTaskList)
//
////        Toast.makeText(this, "${mBoardDetails!!.taskList.size}", Toast.LENGTH_SHORT).show()
////        binding?.rvTaskList?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
////        binding?.rvTaskList?.setHasFixedSize(true)
////        val adapter = TaskAdapter(this, mBoardDetails!!.taskList)
////        binding?.rvTaskList?.adapter = adapter
    }

    fun cardDetails(taskPosition: Int, cardPosition: Int) {
        val intent = Intent(this, CardActivity::class.java)
        intent.putExtra(Constants.BOARD_ID, mBoardDetails!!)
        intent.putExtra(Constants.CARD_LIST_POSITION, cardPosition)
        intent.putExtra(Constants.TASK_LIST_POSITION, taskPosition)
        intent.putExtra(Constants.BOARD_MEMBERS_LIST, mAssignedMembersList)
        startActivity(intent)
    }

    override fun onResume() {
        FirestoreClass().getBoardDetails(this, mBoardDetails!!.boardId!!)
        super.onResume()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_member -> {
                val intent = Intent(this@TaskListActivity , MembersActivity::class.java)
                intent.putExtra(Constants.MEMBERS, mBoardDetails)
                startActivityForResult(intent, MEMBERS_ADD_CODE)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun addCardToTaskList(position: Int, cardName : String) {
        val cardAssignedUserList : ArrayList<String> = ArrayList()
        cardAssignedUserList.add(FirestoreClass().getCurrentUserID())
        Toast.makeText(this, "${cardName}", Toast.LENGTH_SHORT).show()
        val card = Card(cardName,
            FirestoreClass().getCurrentUserID(), cardAssignedUserList)

        mBoardDetails!!.taskList[position].cards.add(0,card)
//        mBoardDetails!!.taskList.removeAt(mBoardDetails!!.taskList.size - 1)
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this, mBoardDetails!!)
    }

    fun deleteTaskList(position: Int) {
        mBoardDetails!!.taskList.removeAt(position)
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this, mBoardDetails!!)
    }

    fun updateTaskList(task: Task, position: Int, listName: String ) {
        val task = Task(listName, FirestoreClass().getCurrentUserID())
        mBoardDetails!!.taskList[position] = task
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this, mBoardDetails!!)
    }

    fun createTaskList(taskListName: String) {
        val task = Task(taskListName, FirestoreClass().getCurrentUserID())
        mBoardDetails!!.taskList.add(0, task)
        Toast.makeText(this, "${mBoardDetails!!.taskList.size.toString()}", Toast.LENGTH_SHORT).show()
//        mBoardDetails!!.taskList.removeAt(mBoardDetails!!.taskList.size - 1)
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this, mBoardDetails!!)
        hideProgressDialog()
    }

    fun addUpdateTaskListSuccess() {
        hideProgressDialog()
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getBoardDetails(this, mBoardDetails!!.boardId!!)
        Toast.makeText(this, "Task List created successfully.", Toast.LENGTH_SHORT).show()
        hideProgressDialog()
    }


    fun setUpBoardDetails(board: Board) {
        hideProgressDialog()

        board.taskList.add(Task(resources.getString(R.string.add_list)))

        binding?.rvTaskList?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding?.rvTaskList?.setHasFixedSize(true)
        val adapter = TaskAdapter(this, board.taskList)
        binding?.rvTaskList?.adapter = adapter

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getMembersFormBoardsList(this, mBoardDetails!!.assignedTo)
    }

    private fun setUpActionBar() {
       setSupportActionBar(binding?.toolbarTaskListActivity)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24)
            supportActionBar!!.title = mBoardDetails!!.name
        }
        binding?.toolbarTaskListActivity?.setNavigationOnClickListener {
            onBackPressed()
        }
    }
}