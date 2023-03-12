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

/**
 * MainActivity is class that controls activity_main.xml that includes drawer layout navigation
 * view that has nav_header_main in header layout and for menu activity_main_drawer. Content is placed
 * in app_bar_main where fib to add new board is placed and tool bar and in other file called
 * activity_main_content is where recycler view for boards is placed
 *
 *  Inherits properties form BaseActivity
 *  @see NavigationView
 *  @see https://developer.android.com/reference/com/google/android/material/navigation/NavigationView
 *  @see https://developer.android.com/guide/navigation/navigation-getting-started
 *  @see https://developer.android.com/reference/androidx/drawerlayout/widget/DrawerLayout
 *  @see https://www.youtube.com/watch?v=_H0afgOSnYc
 */

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var binding: ActivityMainBinding? = null
    private var bindingNavHeaderMain: NavHeaderMainBinding? = null
    private var bindingAppBarMain: AppBarMainBinding? = null
    private var bindingMainContent: ActivityMainContentBinding? = null

    private var mUser: User? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        bindingNavHeaderMain = NavHeaderMainBinding.inflate(layoutInflater)
        bindingAppBarMain = AppBarMainBinding.inflate(layoutInflater)
        bindingMainContent = ActivityMainContentBinding.inflate(layoutInflater)

        setContentView(binding?.root)

        setupActionBar() /** Setting up action bar*/

        binding?.navView?.setNavigationItemSelectedListener(this) /** Listener for navigation */

        FirestoreClass().getUserData(this, true) /** Getting user data */

        /**
         * Listener for floating button to call start contract for adding new board
         */

        binding?.appBarMain?.fabAddNewTask?.setOnClickListener {
            val intent = Intent(this, CreateBoardActivity::class.java)
            intent.putExtra(Constants.USERS, mUser)
            addNewBoardContract.launch(intent)
        }

    }

    /**
     * Contract for adding new board to activity and notifying if board has been added
     * @see onCreate
     * @see CreateBoardActivity
     */

    var addNewBoardContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            FirestoreClass().getBoardsList(this)
        }
    }

    /**
     * Contract for displaying board details or task list from board and notify if change has been made
     * @see setUpBoardRecyclerView
     * @see TaskListActivity
     */

    var boardDetailContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "Deleted Board", Toast.LENGTH_SHORT)
            FirestoreClass().getBoardsList(this)
        }
    }

    /**
     * Function to setup recycler view and it's onclick listener
     * @see BoardAdapter
     * @see FirestoreClass.getBoardsList
     */

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
                    boardDetailContract.launch(intent)
                }
            })

        } else {
            binding?.appBarMain?.mainContent?.rvBoardsList?.visibility = View.GONE
            binding?.appBarMain?.mainContent?.tvNoBoardsAvailable?.visibility = View.VISIBLE
        }
    }

    /**
     * Function is called when we have data to setup drawer view/navigation
     * @see FirestoreClass.getUserData
     */

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
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_profile -> {
                val intent = Intent(this, ProfileActivity::class.java)
//                startActivityForResult(intent, MY_PROFILE_REQUEST_CODE)
                resultLauncher.launch(intent)


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

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            FirestoreClass().getUserData(this,false)
        }
    }
}