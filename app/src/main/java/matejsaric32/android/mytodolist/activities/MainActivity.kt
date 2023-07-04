package matejsaric32.android.mytodolist.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
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

    private lateinit var mShearedPreferences: android.content.SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        bindingNavHeaderMain = NavHeaderMainBinding.inflate(layoutInflater)
        bindingAppBarMain = AppBarMainBinding.inflate(layoutInflater)
        bindingMainContent = ActivityMainContentBinding.inflate(layoutInflater)

        setContentView(binding?.root)

        setupActionBar() /** Setting up action bar*/

        binding?.navView?.setNavigationItemSelectedListener(this) /** Listener for navigation */

        mShearedPreferences = this.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE)

        val tokenUpdated = mShearedPreferences.getBoolean(Constants.FCM_TOKEN_UPDATED, false)

//        val hashMap :HashMap<String, Any> = HashMap()
//        hashMap.put(Constants.FCM_TOKEN, "Matej")
//        FirestoreClass().updateUserData(this, hashMap)
        Log.d("Token", tokenUpdated.toString())
        if (tokenUpdated) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getUserData(this, true)
        }else{
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener(this@MainActivity) {
                    updateFCMToken(it)
                }
        }



//        FirestoreClass().getUserData(this, true) /** Getting user data */

        /**
         * Listener for floating button to call start contract for adding new board
         */

        binding?.appBarMain?.fabAddNewTask?.setOnClickListener {
                            val intent = Intent(this, CreateBoardActivity::class.java)
                intent.putExtra(Constants.USERS, mUser)
                addNewBoardContract.launch(intent)

//            if(FirestoreClass().isOnline(this)) {
//                val intent = Intent(this, CreateBoardActivity::class.java)
//                intent.putExtra(Constants.USERS, mUser)
//                addNewBoardContract.launch(intent)
//            }else{
//                showErrorSnackBar(resources.getString(R.string.offline_info))
//            }
        }

        requestNotificationPermission()

    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
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
        hideProgressDialog()
        if (readBoardsList) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getBoardsList(this)
        }

        binding?.navView?.getHeaderView(0)?.let {
            bindingNavHeaderMain = NavHeaderMainBinding.bind(it)

            if (user.image!!.isNotEmpty()) {
                Glide
                    .with(this)
                    .load(user.image)
                    .centerCrop()
                    .placeholder(R.drawable.ic_baseline_account_circle_24)
                    .into(bindingNavHeaderMain?.sivPlaceImage!!)
            }
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
                resultLauncher.launch(intent)

            }
            R.id.nav_sign_out -> {
                alertDialogForSignOutDeleteList(getString(R.string.sign_out_alert))

            }
        }
        binding?.drawerLayout?.closeDrawer(GravityCompat.START)
        return false
    }

    private fun alertDialogForSignOutDeleteList(title: String) {

        val builderMaterial = MaterialAlertDialogBuilder(this)
            .setTitle("Alert")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setMessage("Are you sure you want to delete $title.")
            .setPositiveButton(Html.fromHtml("<font color='#000000'>Yes</font>")) { dialog, which ->
                dialog.dismiss()
                FirebaseAuth.getInstance().signOut()
                mShearedPreferences.edit().clear().apply()
                val intent = Intent(this, IntroActivity::class.java)
                startActivity(intent)

            }
            .setNegativeButton(Html.fromHtml("<font color='#000000'>No</font>")){ dialog, which ->
                dialog.dismiss()}
            .create()

        builderMaterial.setCancelable(false)
        builderMaterial.show()
    }

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show()
            mUser = result.data?.getParcelableExtra(Constants.USERS)!!
            updateNavigationUserDetails(mUser!!, false)
        }
    }

    fun tokenUpdateSuccess(){
        hideProgressDialog()
        val editor: SharedPreferences.Editor = mShearedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED, true)
        editor.apply()
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getUserData(this, true)
    }

    private fun updateFCMToken(token: String){
        val userHashMap = HashMap<String, Any>()
        userHashMap[Constants.FCM_TOKEN] = token
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().updateUserData(this, userHashMap)

    }
}