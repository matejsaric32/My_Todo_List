package matejsaric32.android.mytodolist.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import matejsaric32.android.mytodolist.R
import matejsaric32.android.mytodolist.databinding.ActivityLogInBinding
import matejsaric32.android.mytodolist.firebase.FirestoreClass

class LogInActivity : BaseActivity() {

    private var binding: ActivityLogInBinding? = null
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("168470476939-h51c6vj0ued37nr2r1o4ej31mlhe91i1.apps.googleusercontent.com")
        .requestEmail()
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        firebaseAuth = FirebaseAuth.getInstance() /** Getting a instance of Firebase Authentication  */
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
        setupActionBar() /** Setting up action bar */

        /**
         * Listener when button is clicked calls signInUser function.
         */

        binding?.btnSignIn?.setOnClickListener {
            signInUser()
        }

        binding?.btnGoogleSignIn?.setOnClickListener {
            Log.d("GoogleSignIn", "firebaseAuthWithGoogle: 1")

            signInUserGoogle()
        }


    }

    private fun signInUserGoogle() {
        Log.d("GoogleSignIn", "firebaseAuthWithGoogle: 2")

        val signInIntent = googleSignInClient.signInIntent
        signWithGoggle.launch(signInIntent)
    }

    private val signWithGoggle = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { resault ->
        showProgressDialog(resources.getString(R.string.please_wait))
        if (resault.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(resault.data)
            Log.d("GoogleSignIn", "firebaseAuthWithGoogle:" + resault.data.toString())
            if (task.isSuccessful) {
                val account = task.result
                if (account != null) {
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    Log.d("GoogleSignIn", "firebaseAuthWithGoogle:" + account.idToken)
                    firebaseAuth.signInWithCredential(credential)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                FirestoreClass().getUserData(this)
                            } else {
                                Toast.makeText(this, "Unable to login!!!", Toast.LENGTH_SHORT)
                            }
                        }
                }
            } else {
                Toast.makeText(this, "Unable to login!!!", Toast.LENGTH_SHORT)
            }

//            try {
//                val account = task.getResult(ApiException::class.java)
//                firebaseAuthWithGoogle(account.idToken!!)
//            } catch (e: ApiException) {
//                Log.w("GoogleSignIn", "Google sign in failed", e)
//            }
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