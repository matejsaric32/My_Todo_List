package matejsaric32.android.mytodolist.activities

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import matejsaric32.android.mytodolist.R
import matejsaric32.android.mytodolist.adapter.MemberAdapter
import matejsaric32.android.mytodolist.databinding.ActivityMembersBinding
import matejsaric32.android.mytodolist.databinding.DialogAddMembersBinding
import matejsaric32.android.mytodolist.firebase.FirestoreClass
import matejsaric32.android.mytodolist.models.Board
import matejsaric32.android.mytodolist.models.User
import matejsaric32.android.mytodolist.utils.Constants

class MembersActivity : BaseActivity() {

    private var binding: ActivityMembersBinding? = null
    private var mBoardDetails: Board? = null

    private var mAssignedMembersList: ArrayList<User> = ArrayList()

    private lateinit var mAdapter: MemberAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMembersBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        if (intent.hasExtra(Constants.MEMBERS)) {
            mBoardDetails = intent.getParcelableExtra<Board>(Constants.MEMBERS) as Board
            Log.e("Board Name", mBoardDetails!!.name!!)
        }

        setUpActionBar()

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getMembersFormBoardsList(this, mBoardDetails!!.assignedTo)
    }

    fun memberAdded(user: User) {
        hideProgressDialog()
        mAssignedMembersList.add(user)
        mAdapter.notifyDataSetChanged()
        Toast.makeText(this, "Member added successfully", Toast.LENGTH_SHORT).show()
    }

    fun userDetails(user: User) {
        hideProgressDialog()
        mBoardDetails!!.assignedTo.add(user.id!!)
        FirestoreClass().assignMemberToBoard(this, mBoardDetails!!, user)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_member, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_member -> {
                displayMemberSearchDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun displayMemberSearchDialog() {
        val dialog = Dialog(this)
        val binding = DialogAddMembersBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)
        binding.tvAdd.setOnClickListener {
            val email = binding.etEmailSearchMember.text.toString()
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

    fun setUpMembersList(membersList: ArrayList<User>) {
        hideProgressDialog()
        mAssignedMembersList = membersList


        if (membersList.size > 0) {

            binding?.rvMembersList?.layoutManager = LinearLayoutManager(this)
            binding?.rvMembersList?.setHasFixedSize(true)

            mAdapter = MemberAdapter(this, membersList)
            binding?.rvMembersList?.adapter = mAdapter
        } else {
            binding?.rvMembersList?.visibility = View.GONE
        }
    }

    private fun setUpActionBar() {
        setSupportActionBar(binding?.toolbarMembersActivity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24)
            actionBar.title = "Members of:  ${mBoardDetails!!.name!!}"
        }
        binding?.toolbarMembersActivity?.setNavigationOnClickListener { onBackPressed() }
    }
}