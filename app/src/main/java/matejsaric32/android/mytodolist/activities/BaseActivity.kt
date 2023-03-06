package matejsaric32.android.mytodolist.activities

import android.app.Dialog
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import matejsaric32.android.mytodolist.R
import matejsaric32.android.mytodolist.databinding.ActivityBaseBinding
import matejsaric32.android.mytodolist.databinding.DialogProgressBinding

open class BaseActivity : AppCompatActivity() {

    private var doubleBackToExitPressedOnce = false

    var bindingBase: ActivityBaseBinding? = null

    private lateinit var mProgressDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindingBase = ActivityBaseBinding.inflate(layoutInflater)

        setContentView(bindingBase?.root)
    }

    fun showProgressDialog(text: String) {
        mProgressDialog = Dialog(this)
        val binding = DialogProgressBinding.inflate(layoutInflater)
        mProgressDialog!!.setContentView(binding.root)
        binding.tvProgressText.text = text
        mProgressDialog.show()
    }

    fun hideProgressDialog() {
        mProgressDialog.dismiss()
    }

    fun getCurrentUserID(): String {
        return FirebaseAuth.getInstance().currentUser!!.uid
    }

    fun doubleBackToExit() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(
            this,
            "Please click BACK again to exit",
            Toast.LENGTH_SHORT
        ).show()

        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)

    }

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