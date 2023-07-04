package matejsaric32.android.mytodolist.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import matejsaric32.android.mytodolist.R
import matejsaric32.android.mytodolist.databinding.ActivityIntroBinding
import matejsaric32.android.mytodolist.firebase.FirestoreClass

class IntroActivity : BaseActivity() {

    private var binding: ActivityIntroBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        /**
         * Listener when is clicked starts LogInActivity
         */

        binding?.btnSignIn?.setOnClickListener {
            if(FirestoreClass().isOnline(this)) {
                val intent = Intent(this, LogInActivity::class.java)
                startActivity(intent)
            }else{
                showErrorSnackBar(resources.getString(R.string.offline_info))
            }
        }

        /**
         * Listener when is clicked starts RegisterActivity
         */

        binding?.btnSignUp?.setOnClickListener {
            if(FirestoreClass().isOnline(this)) {
                val intent = Intent(this, RegisterActivity::class.java)
                startActivity(intent)
            }else{
                showErrorSnackBar(resources.getString(R.string.offline_info))
            }
        }
    }
}