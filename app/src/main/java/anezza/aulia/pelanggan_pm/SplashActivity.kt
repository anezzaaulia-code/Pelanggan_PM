package anezza.aulia.pelanggan_pm

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import anezza.aulia.pelanggan_pm.databinding.ActivitySplashBinding
import anezza.aulia.pelanggan_pm.helper.SessionManager

class SplashActivity : AppCompatActivity() {

    private lateinit var b: ActivitySplashBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(b.root)

        session = SessionManager(this)

        Handler(Looper.getMainLooper()).postDelayed({
            if (session.isLogin()) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        }, 1500)
    }
}