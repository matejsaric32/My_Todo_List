package matejsaric32.android.mytodolist.activities

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import matejsaric32.android.mytodolist.R
import matejsaric32.android.mytodolist.adapter.BoardAdapter
import matejsaric32.android.mytodolist.databinding.ActivityMainBinding
import matejsaric32.android.mytodolist.databinding.ActivityMainContentBinding
import matejsaric32.android.mytodolist.databinding.AppBarMainBinding
import matejsaric32.android.mytodolist.databinding.NavHeaderMainBinding
import matejsaric32.android.mytodolist.firebase.FirestoreClass
import matejsaric32.android.mytodolist.models.Board
import matejsaric32.android.mytodolist.models.User
import matejsaric32.android.mytodolist.utils.Constants

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var binding: ActivityMainBinding? = null
    private var bindingNavHeaderMain: NavHeaderMainBinding? = null
    private var bindingAppBarMain: AppBarMainBinding? = null
    private var bindingMainContent: ActivityMainContentBinding? = null

    private var mUser: User? = null

    companion object {
        const val MY_PROFILE_REQUEST_CODE: Int = 55
        const val CREATE_BOARD_REQUEST_CODE: Int = 66
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        bindingNavHeaderMain = NavHeaderMainBinding.inflate(layoutInflater)
        bindingAppBarMain = AppBarMainBinding.inflate(layoutInflater)
        bindingMainContent = ActivityMainContentBinding.inflate(layoutInflater)

        setContentView(binding?.root)

        setupActionBar()

        binding?.navView?.setNavigationItemSelectedListener(this)

        FirestoreClass().getUserData(this, true)

        binding?.appBarMain?.fabAddNewTask?.setOnClickListener {
            val intent = Intent(this, CreateBoardActivity::class.java)
            intent.putExtra(Constants.USERS, mUser)
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
        }

    }

    fun setUpBoardRecyclerView(boardList: ArrayList<Board>) {
        hideProgressDialog()

        if (boardList.size > 0) {
            binding?.appBarMain?.mainContent?.rvBoardsList?.visibility = View.VISIBLE
            binding?.appBarMain?.mainContent?.tvNoBoardsAvailable?.visibility = View.GONE

            binding?.appBarMain?.mainContent?.rvBoardsList?.layoutManager = LinearLayoutManager(this)
            binding?.appBarMain?.mainContent?.rvBoardsList?.setHasFixedSize(true)

            val adapter = BoardAdapter(this, boardList)
            binding?.appBarMain?.mainContent?.rvBoardsList?.adapter = adapter

            adapter.setOnClickListener(object : BoardAdapter.OnClickListener {
                override fun onClick(position: Int, model: Board) {
                    val intent = Intent(this@MainActivity, TaskListActivity::class.java)
                    intent.putExtra(Constants.BOARD_ID, model)
                    startActivity(intent)
                }
            })

        } else {
            binding?.appBarMain?.mainContent?.rvBoardsList?.visibility = View.GONE
            binding?.appBarMain?.mainContent?.tvNoBoardsAvailable?.visibility = View.VISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK
            && requestCode == MY_PROFILE_REQUEST_CODE
        ) {
            FirestoreClass().getUserData(this@MainActivity)
        } else if (resultCode == Activity.RESULT_OK
            && requestCode == CREATE_BOARD_REQUEST_CODE
        ) {
            FirestoreClass().getBoardsList(this@MainActivity)
        } else {
            Log.e("Cancelled", "Cancelled")
        }
    }

    override fun onResume() {
        super.onResume()
        FirestoreClass().getUserData(this,false)
    }

    fun updateNavigationUserDetails(user: User, readBoardsList: Boolean){
        Toast.makeText(this, "User: ${user.name}", Toast.LENGTH_SHORT).show()

        if (readBoardsList) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getBoardsList(this)
        }

        binding?.navView?.getHeaderView(0)?.let {
            bindingNavHeaderMain = NavHeaderMainBinding.bind(it)

            Glide
                .with(this)
                .load(user.image)
                .centerCrop()
                .placeholder(R.drawable.ic_baseline_account_circle_24)
                .into(bindingNavHeaderMain?.sivPlaceImage!!)

            bindingNavHeaderMain?.tvUsername?.text = user.name
            mUser = user!!
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(binding?.appBarMain?.toolbarMainActivity)

        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = "My To Do List"
            binding?.appBarMain?.toolbarMainActivity?.setNavigationIcon(R.drawable.ic_baseline_menu_24)
        }

        binding?.appBarMain?.toolbarMainActivity?.setNavigationOnClickListener {
            toggleDrawer()
        }



    }

    private fun toggleDrawer() {
        if (binding?.drawerLayout?.isDrawerOpen(GravityCompat.START)!!) {
            binding?.drawerLayout?.closeDrawer(GravityCompat.START)
        } else {
            binding?.drawerLayout?.openDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed() {
        if (binding?.drawerLayout?.isDrawerOpen(GravityCompat.START)!!) {
            binding?.drawerLayout?.closeDrawer(GravityCompat.START)
        } else {
            doubleBackToExit()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_profile -> {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivityForResult(intent, MY_PROFILE_REQUEST_CODE)

            }
            R.id.nav_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, IntroActivity::class.java)
                startActivity(intent)
            }
        }
        binding?.drawerLayout?.closeDrawer(GravityCompat.START)
        return false
    }
}