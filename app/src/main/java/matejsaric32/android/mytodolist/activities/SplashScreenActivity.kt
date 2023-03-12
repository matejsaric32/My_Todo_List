package matejsaric32.android.mytodolist.activities

import android.content.Intent
import android.content.Intent.getIntent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import matejsaric32.android.mytodolist.R
import matejsaric32.android.mytodolist.databinding.ActivitySplashScreenBinding
import matejsaric32.android.mytodolist.firebase.FirestoreClass
import matejsaric32.android.mytodolist.utils.Constants

class SplashScreenActivity : BaseActivity() {

    private var binding: ActivitySplashScreenBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        /**
         * Show splash screen for x amount of time then go to Intro activity if
         * there is no user signed in if there is start MainActivity
         */

        Handler(Looper.getMainLooper()).postDelayed({

            var currentUser = FirestoreClass().getCurrentUserID()

            if(!currentUser.isNullOrEmpty()) {
                startActivity(Intent(this, MainActivity::class.java))
            }else{
                startActivity(Intent(this, IntroActivity::class.java))
            }
            finish()
        }, Constants.SPLASH_SCREEN_SHOW_TIMER)
    }
}