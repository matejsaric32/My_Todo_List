package matejsaric32.android.mytodolist.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import matejsaric32.android.mytodolist.R
import matejsaric32.android.mytodolist.databinding.ActivityLogInBinding
import matejsaric32.android.mytodolist.firebase.FirestoreClass
import matejsaric32.android.mytodolist.models.User

class LogInActivity : BaseActivity() {

    private var binding: ActivityLogInBinding? = null
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        firebaseAuth = FirebaseAuth.getInstance() /** Getting a instance of Firebase Authentication  */
        setupActionBar() /** Setting up action bar */

        /**
         * Listener when button is clicked calls signInUser function.
         */

        binding?.btnSignIn?.setOnClickListener {
            signInUser()
        }

    }

    /**
     * A function for actionBar Setup.
     */

    private fun setupActionBar(){
        setSupportActionBar(binding?.toolbarSignInActivity)

        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24)
        }

        binding?.toolbarSignInActivity?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    /**
     * A function is called when sign in was successful
     */

    fun signInSuccess(){
        hideProgressDialog()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    /**
     * Function sign in user
     */

    private fun signInUser(){

        val email: String = binding?.etEmail?.text.toString().trim()
        val password: String = binding?.etPassword?.text.toString().trim()

        if(validateForm(email, password)){
            showProgressDialog(resources.getString(R.string.please_wait))
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(
                    OnCompleteListener<AuthResult> { task ->
                        hideProgressDialog()
                        if (task.isSuccessful) {
                            FirestoreClass().getUserData(this)
                            Log.i("UserLogin", "User logged in successfully")
                        } else {
                            Log.e("UserLogin", "User logged was unsuccessfully")
                            Toast.makeText(this, "Unable to login!!!", Toast.LENGTH_SHORT)
                        }
                    }
                )
        }

    }

    /**
     * Function to validate form
     */

    private fun validateForm(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                showErrorSnackBar("Please enter an email address.")
                false
            }
            password.isEmpty() -> {
                showErrorSnackBar("Please enter a password.")
                false
            }
            else -> {
                true
            }
        }
    }
}