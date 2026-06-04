package anezza.aulia.pelanggan_pm

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import anezza.aulia.pelanggan_pm.databinding.ActivityMainBinding
import anezza.aulia.pelanggan_pm.fragment.BerandaFragment
import anezza.aulia.pelanggan_pm.fragment.KeranjangFragment
import anezza.aulia.pelanggan_pm.fragment.PesananFragment
import anezza.aulia.pelanggan_pm.fragment.ProdukFragment
import anezza.aulia.pelanggan_pm.fragment.ProfilFragment
import anezza.aulia.pelanggan_pm.helper.SessionManager

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        session = SessionManager(this)

        openFragment(BerandaFragment())

        b.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navBeranda -> openFragment(BerandaFragment())
                R.id.navProduk -> openFragment(ProdukFragment())
                R.id.navKeranjang -> openFragment(KeranjangFragment())
                R.id.navPesanan -> openFragment(PesananFragment())
                R.id.navProfil -> openFragment(ProfilFragment())
            }
            true
        }
    }

    private fun openFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameContainer, fragment)
            .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuRefresh -> recreate()
            R.id.menuLogout -> {
                session.logout()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
        return true
    }
}