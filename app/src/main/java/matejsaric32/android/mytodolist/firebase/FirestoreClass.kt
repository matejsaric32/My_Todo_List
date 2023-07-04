package matejsaric32.android.mytodolist.firebase

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import matejsaric32.android.mytodolist.activities.*
import matejsaric32.android.mytodolist.models.Board
import matejsaric32.android.mytodolist.models.User
import matejsaric32.android.mytodolist.utils.Constants


class FirestoreClass {

    data class Test(val name: String, val age: Int)

    private var fireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity: RegisterActivity, userInfo: User) {

        fireStore.collection(Constants.USERS)
        .document(getCurrentUserID())
        .set(userInfo, SetOptions.merge())
        .addOnSuccessListener {
            activity.userRegisteredSuccess()
            Log.e("registerUser", "Success")
        }
        .addOnFailureListener { e ->
            activity.hideProgressDialog()
            Log.e(activity.javaClass.simpleName, "Error while registering the user.", e)
        }


    }

    fun getCurrentUserID(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserHelper = ""
        if (currentUser != null) {
            currentUserHelper = currentUser.uid
        }

        return currentUserHelper
    }

    fun getUserData(activity: Activity, readBoardsList: Boolean = false){
        fireStore = FirebaseFirestore.getInstance()
        fireStore.collection(Constants.USERS)
            .document(getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val loggedUser = document.toObject(User::class.java)

                    when(activity){
                        is LogInActivity -> {
                            activity.signInSuccess()
                        }
                        is MainActivity -> {
                            Log.e("LoginDataMain", "DocumentSnapshot data: ${document.data}")
                            activity.updateNavigationUserDetails(loggedUser!!, readBoardsList)
                        }
                        is ProfileActivity -> {
                            activity.setUserData(loggedUser!!)
                        }
                    }

                    Log.d("LoginData", "DocumentSnapshot data: ${document.data}")
                } else {
                    Log.d("LoginData", "No such document")
                }

            }
            .addOnFailureListener { exception ->
                when(activity){
                    is LogInActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                    is ProfileActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.d("TAG", "get failed with ", exception)
            }
    }

    fun updateUserData(activity: Activity, userHashMap: HashMap<String, Any>){
        fireStore.collection(Constants.USERS)
            .document(getCurrentUserID())
            .update(userHashMap)
            .addOnSuccessListener {
                Log.i("UpdateUserData", "User profile data updated successfully.")

                when(activity){
                    is ProfileActivity -> {
                        activity.updateSuccess()
                    }
                    is MainActivity -> {
                        activity.tokenUpdateSuccess()
                    }
                }
            }
            .addOnFailureListener { e ->
                when(activity){
                    is ProfileActivity -> {
                        activity.updateFailure()
                    }
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e("UpdateUserData", "Error while creating a board.", e)
            }
    }

    fun createNewBoard(activity: CreateBoardActivity, boardInfo: Board){

        fireStore.collection(Constants.BOARDS)
            .document()
            .set(boardInfo, SetOptions.merge())
            .addOnSuccessListener {
                Log.e("CreateBoard", "Board created successfully.")
                activity.boardCreatedSuccessfully()
            }
            .addOnFailureListener { e ->
                activity.boardCreatedUnsuccessfully()
                Log.e("CreateBoard", "Error while creating a board.", e)
            }

        SystemClock.sleep(2000)
        activity.boardCreatedSuccessfully()

    }

    fun getBoardsList(activity: MainActivity){

        fireStore.collection(Constants.BOARDS)
            .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                Log.e("BoardsList", document.documents.toString())
                val boardsList: ArrayList<Board> = ArrayList()
                for (element in document.documents){
                    val board = element.toObject(Board::class.java)!!
                    board.boardId = element.id
                    boardsList.add(board)
                }
                Log.d("BoardsWidget", "updateAppWidget: " + boardsList.size)

                activity.setUpBoardRecyclerView(boardsList)
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)
            }
    }

    public val widgetApi = fireStore.collection(Constants.BOARDS)
        .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserID())


    fun getBoardDetails(activity: Activity, boardId: String){

        if (isOnline(activity)){

            fireStore.enableNetwork().addOnCompleteListener{
                fireStore.collection(Constants.BOARDS)
                    .document(boardId)
                    .get()
                    .addOnSuccessListener { document ->
                        Log.e("BoardsId", document.toString())
                        val board = document.toObject(Board::class.java)!!
                        board.boardId = document.id

                        when(activity){
                            is TaskListActivity -> {
                                activity.setUpBoardDetails(board)
                            }

                        }
                    }
                    .addOnFailureListener { e ->
                        when(activity){
                            is TaskListActivity -> {
                                activity.hideProgressDialog()
                            }
                        }
                        Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)
                    }
            }
        }else{


            fireStore.disableNetwork().addOnCompleteListener{
                fireStore.collection(Constants.BOARDS)
                    .document(boardId)
                    .get()
                    .addOnSuccessListener { document ->
                        Log.e("BoardsId", document.toString())
                        val board = document.toObject(Board::class.java)!!
                        board.boardId = document.id

                        when(activity){
                            is TaskListActivity -> {
                                activity.setUpBoardDetails(board)
                            }

                        }
                    }
                    .addOnFailureListener { e ->
                        when(activity){
                            is TaskListActivity -> {
                                activity.hideProgressDialog()
                            }
                        }
                        Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)
                    }
            }
        }

    }

    fun addUpdateTaskList(activity: BaseActivity, boardInfo: Board, actionType: String = ""){
        val taskListHashMap = HashMap<String, Any>()
        taskListHashMap[Constants.TASK_LIST] = boardInfo.taskList

        if (activity is UpdateBoardActivity) {
            taskListHashMap.put(Constants.IMAGE, boardInfo.image!!)
            taskListHashMap.put(Constants.NAME, boardInfo.name!!)
        }

        if (activity is MembersActivity) {
            taskListHashMap.put(Constants.ASSIGNED_TO, boardInfo.assignedTo)
        }

        if (isOnline(activity)) {
            fireStore.enableNetwork().addOnCompleteListener{
                fireStore.collection(Constants.BOARDS)
                    .document(boardInfo.boardId!!)
                    .update(taskListHashMap)
                    .addOnSuccessListener {
                        Log.i("TaskListUpdate", "TaskList updated successfully.")

                        when(activity){
                            is TaskListActivity -> {
                                Log.i("TaskListUpdate - TaskListActivity", "TaskListActivity")
                                when(actionType){
                                    Constants.ACTIONS_TAKEN_DELETE -> {
                                        activity.addUpdateTaskListSuccess(false, Constants.ACTIONS_TAKEN_DELETE)
                                    }
                                    Constants.ACTIONS_TAKEN_INSERT -> {
                                        activity.addUpdateTaskListSuccess(false, Constants.ACTIONS_TAKEN_INSERT)
                                    }
                                    Constants.ACTIONS_TAKEN_UPDATE -> {
                                        activity.addUpdateTaskListSuccess(false, Constants.ACTIONS_TAKEN_UPDATE)
                                    }
                                    Constants.ACTIONS_TAKEN_INSERT_CARD -> {
                                        activity.addUpdateTaskListSuccess(false, Constants.ACTIONS_TAKEN_INSERT_CARD)
                                    }
                                }

                                activity.addUpdateTaskListSuccess()
                            }
                            is CardActivity -> {
                                when(actionType){
                                    Constants.ACTIONS_TAKEN_DELETE_CARD -> {
                                        activity.updatedeleteCardSuccess(Constants.ACTIONS_TAKEN_DELETE_CARD)
                                    }
                                    Constants.ACTIONS_TAKEN_UPDATE_CARD -> {
                                        activity.updatedeleteCardSuccess(Constants.ACTIONS_TAKEN_UPDATE_CARD)
                                    }
                                }

                                Log.i("TaskListUpdate - CardActivity", "CardActivity")
                            }
                            is UpdateBoardActivity -> {
                                activity.updateBoardSuccess()
                                Log.i("UpdateBoardActivity", "UpdateBoardActivity")
                            }
                            is MembersActivity -> {
                                activity.updateSuccess()
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        when (activity) {
                            is TaskListActivity -> {
                                activity.hideProgressDialog()
                                Log.i("TaskListUpdate - TaskListActivity", "Failed to update update board")
                                activity.hideProgressDialog()
                            }
                            is CardActivity -> {
                                activity.hideProgressDialog()
                                activity.updatedeleteCardSuccess()
                                Log.i("TaskListUpdate - CardActivity", "Failed to update update board")
                            }
                            is UpdateBoardActivity -> {
                                activity.updateBoardFailure()
                                Log.e("UpdateBoardActivity", "Failed to update update board")
                            }
                        }
                    }
            }
        }else{

            when(activity){
                is TaskListActivity -> {
                    Log.i("TaskListUpdate - TaskListActivity1", "TaskListActivity")
                    activity.addUpdateTaskListSuccess(true)
                }
                is CardActivity -> {
                    activity.updatedeleteCardSuccess()
                    Log.i("TaskListUpdate - CardActivity", "CardActivity")
                }
                is UpdateBoardActivity -> {
                    activity.updateBoardSuccess()
                    Log.i("UpdateBoardActivity", "UpdateBoardActivity")
                }
            }

            fireStore.disableNetwork().addOnCompleteListener{
                fireStore.collection(Constants.BOARDS)
                    .document(boardInfo.boardId!!)
                    .update(taskListHashMap)
                    .addOnSuccessListener {
                        Log.i("TaskListUpdate", "TaskList updated successfully.")

                        when(activity){
                            is TaskListActivity -> {
                                Log.i("TaskListUpdate - TaskListActivity", "TaskListActivity")
                                activity.addUpdateTaskListSuccess()
                            }
                            is CardActivity -> {
                                activity.updatedeleteCardSuccess()
                                Log.i("TaskListUpdate - CardActivity", "CardActivity")
                            }
                            is UpdateBoardActivity -> {
                                activity.updateBoardSuccess()
                                Log.i("UpdateBoardActivity", "UpdateBoardActivity")
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        when (activity) {
                            is TaskListActivity -> {
                                activity.hideProgressDialog()
                                Log.i("TaskListUpdate - TaskListActivity", "Failed to update update board")
                                activity.addUpdateTaskListSuccess()
                            }
                            is CardActivity -> {
                                activity.hideProgressDialog()
                                activity.updatedeleteCardSuccess()
                                Log.i("TaskListUpdate - CardActivity", "Failed to update update board")
                            }
                            is UpdateBoardActivity -> {
                                activity.updateBoardFailure()
                                Log.e("UpdateBoardActivity", "Failed to update update board")
                            }
                        }

                    }
            };
        }
    }

    fun getMembersFormBoardsList(activity: Activity, boardMembersId: ArrayList<String>){
        if (boardMembersId.isEmpty()) {
            activity.finish()
            return
        }
        fireStore.collection(Constants.USERS)
            .whereIn(Constants.ID, boardMembersId)
            .get()
            .addOnSuccessListener { document ->
                Log.e("MembersList", document.toString())
                val usersList: ArrayList<User> = ArrayList()
                for (element in document.documents){
                    val user = element.toObject(User::class.java)!!
                    usersList.add(user)
                }
                when(activity){
                    is MembersActivity -> {
                        activity.setUpMembersList(usersList)
                    }
                    is TaskListActivity -> {
                        activity.boardMembersDetails(usersList)
                    }
                }
            }
            .addOnFailureListener { e ->
                when(activity){
                    is MembersActivity -> {
                        activity.failureMembersList()
                    }
                    is TaskListActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e("MembersList", "Error while creating a board.", e)
            }
    }

    fun getMemberDetails(activity: MembersActivity, email: String){
        fireStore.collection(Constants.USERS)
            .whereEqualTo(Constants.EMAIL, email)
            .get()
            .addOnSuccessListener { document ->
                Log.e("MemberDetails", document.toString())
                if (document.documents.size > 0){
                    val user = document.documents[0].toObject(User::class.java)!!
                    activity.userDetails(user)
                }else{
                    activity.hideProgressDialog()
                    Toast.makeText(activity, "No such member found.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e("MemberDetails", "Error while creating a board.", e)
            }
    }

    fun assignMemberToBoard(activity: MembersActivity, board: Board, user: User){
        val assignedToHashMap = HashMap<String, Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = board.assignedTo

        fireStore.collection(Constants.BOARDS)
            .document(board.boardId!!)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                activity.memberAdded(user)
            }
            .addOnFailureListener { e ->
                activity.memberAddedFaliure()
                Log.e("assignMember", "Error while updating users in a board.", e)
            }
    }

    fun deleteBoard(activity: Activity, boardInfo: Board){
        fireStore.collection(Constants.BOARDS)
            .document(boardInfo.boardId!!)
            .delete()
            .addOnSuccessListener {
                when(activity){
                    is TaskListActivity -> {
                        activity.deleteBoardSucces()
                    }
                }
            }

    }

    fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
        }
        return false
    }

}