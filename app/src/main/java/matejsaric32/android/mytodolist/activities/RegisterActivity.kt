package matejsaric32.android.mytodolist.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import matejsaric32.android.mytodolist.R
import matejsaric32.android.mytodolist.databinding.ActivityRegisterBinding
import matejsaric32.android.mytodolist.firebase.FirestoreClass
import matejsaric32.android.mytodolist.models.User
import matejsaric32.android.mytodolist.utils.Constants

/**
 * RegisterActivity is class that controls activity_register.xml
 * Main task of this activity is to register a new user
 * Function inherits BaseActivity
 */


class RegisterActivity : BaseActivity() {

    var binding: ActivityRegisterBinding? = null
    private lateinit var firebaseAuth: FirebaseAuth
    private var isGoogleAuth: Boolean = false
    private lateinit var googleSignInClient: GoogleSignInClient
    private val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("168470476939-h51c6vj0ued37nr2r1o4ej31mlhe91i1.apps.googleusercontent.com")
        .requestEmail()
        .requestProfile()
        .build()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        firebaseAuth = FirebaseAuth.getInstance() /** Getting a instance of Firebase Authentication  */
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
        setupActionBar() /** Setting up action bar */

        /**
         * Listener when button is clicked calls register function.
         */

        binding?.btnSignUp?.setOnClickListener {
            registerUser()
        }

        binding?.btnGoogleSignUp?.setOnClickListener {
            Log.d("GoogleRegisterIn", "firebaseAuthWithGoogle: 1")
            registerUserWithGoogle()
        }

    }

    private fun registerUserWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        signWithGoggle.launch(signInIntent)
    }

    private val signWithGoggle = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { resault ->
        showProgressDialog(resources.getString(R.string.please_wait))
        if (resault.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(resault.data)
            Log.d("GoogleRegisterIn", "firebaseAuthWithGoogle:" + resault.data.toString())
            if (task.isSuccessful) {
                val account = task.result
                if (account != null) {
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    Log.d("GoogleRegisterIn", "firebaseAuthWithGoogle:" + account.displayName)

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
                                        FirestoreClass().registerUser(
                                            this@RegisterActivity,
                                            User(
                                                account.id!!,
                                                account.displayName!!,
                                                account.email!!
                                            )
                                        )
                                    } else {
                                        Toast.makeText(
                                            this,
                                            "Unable to login!!!",
                                            Toast.LENGTH_SHORT
                                        )
                                    }
                                }
                        }


                    }
                } else {
                    Toast.makeText(this, "Unable to login!!!", Toast.LENGTH_SHORT)
                }
            }
        }
    }

    /**
     * A function for actionBar Setup.
     */

    private fun setupActionBar() {
        setSupportActionBar(binding?.toolbarSignUpActivity)

        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24)
        }

        binding?.toolbarSignUpActivity?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    /**
     * Function is called when user registration was successful
     */

    fun userRegisteredSuccess() {
        Toast.makeText(
            this@RegisterActivity,
            "You have successfully registered.",
            Toast.LENGTH_SHORT
        ).show()

        if (isGoogleAuth) {

        } else {
            val intent = Intent(this@RegisterActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    /**
     * Function to register new user
     */

    private fun registerUser() {
        val name: String = binding?.etName?.text.toString().trim()
        val email: String = binding?.etEmail?.text.toString().trim()
        val password: String = binding?.etPassword?.text.toString().trim()

        if (validateForm(name, email, password)) {
            showProgressDialog(resources.getString(R.string.please_wait))
           firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(
                    OnCompleteListener<AuthResult> { task ->
                        hideProgressDialog()
                        if (task.isSuccessful) {

                            Log.i("userRegistration", "User registration was successful; user_id :  " +
                                    "${FirebaseAuth.getInstance().currentUser!!.uid}")

                            val firebaseUser : FirebaseUser = task.result!!.user!!
                            val registeredEmail = firebaseUser.email!!

                            val user = User(firebaseUser.uid, name, registeredEmail)
                            FirestoreClass().registerUser(this@RegisterActivity, user)

                        } else {
                            Log.e("UserLogin", "User logged was unsuccessfully")
                            Toast.makeText(this@RegisterActivity, "Unable to register!!!", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
        }
    }

    /**
     * Function to validate form
     */

    private fun validateForm(name: String, email: String, password: String): Boolean {
        return when {
            name.isEmpty() -> {
                showErrorSnackBar("Please enter a name.")
                false
            }
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