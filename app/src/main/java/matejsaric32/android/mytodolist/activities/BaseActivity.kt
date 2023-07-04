package matejsaric32.android.mytodolist.activities

import android.app.Dialog
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import matejsaric32.android.mytodolist.R
import matejsaric32.android.mytodolist.databinding.ActivityBaseBinding
import matejsaric32.android.mytodolist.databinding.DialogProgressBinding

/**
 * Base activity is activity that has few functions that all function that inherit base function
 * can use as propreties
 */

open class BaseActivity : AppCompatActivity() {

    var bindingBase: ActivityBaseBinding? = null

    private lateinit var mProgressDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingBase = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(bindingBase?.root)
    }

    /**
     * Function to show progress dialog
     * @param text - text to bew diplayed
     */

    fun showProgressDialog(text: String) {
        mProgressDialog = Dialog(this)
        val binding = DialogProgressBinding.inflate(layoutInflater)
        mProgressDialog!!.setContentView(binding.root)
        binding.tvProgressText.text = text
        mProgressDialog.show()
    }

    /**
     * Function to dismiss progress dialog
     */

    fun hideProgressDialog() {
        mProgressDialog.dismiss()
    }

    /**
     * Function that returns current user id
     * @return - current users id
     * @see CreateBoardActivity.createBoard
     */

    fun getCurrentUserID(): String {
        return FirebaseAuth.getInstance().currentUser!!.uid
    }

    /**
     * Function has main task to create a error snack bar
     * @param message - message that will be displayed in snack-bar
     */

    fun showErrorSnackBar(message: String) {
        val snackBar =
            Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
        val snackBarView = snackBar.view
        snackBarView.setBackgroundColor(
            ContextCompat.getColor(
                this@BaseActivity,
                R.color.highlight_accent_2
            )
        )
        snackBar.show()
    }
}