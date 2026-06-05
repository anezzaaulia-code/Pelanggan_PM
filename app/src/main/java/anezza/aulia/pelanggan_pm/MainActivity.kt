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
import anezza.aulia.pelanggan_pm.helper.ThemeHelper

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private lateinit var session: SessionManager

    private val berandaFragment = BerandaFragment()
    private val produkFragment = ProdukFragment()
    private val keranjangFragment = KeranjangFragment()
    private val pesananFragment = PesananFragment()
    private val profilFragment = ProfilFragment()

    private var activeFragment: Fragment = berandaFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)

        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        session = SessionManager(this)

        setupFragments()
        setupBottomNavigation()
    }

    private fun setupFragments() {
        supportFragmentManager.beginTransaction()
            .add(R.id.frameContainer, profilFragment, "profil")
            .hide(profilFragment)
            .add(R.id.frameContainer, pesananFragment, "pesanan")
            .hide(pesananFragment)
            .add(R.id.frameContainer, keranjangFragment, "keranjang")
            .hide(keranjangFragment)
            .add(R.id.frameContainer, produkFragment, "produk")
            .hide(produkFragment)
            .add(R.id.frameContainer, berandaFragment, "beranda")
            .commit()

        activeFragment = berandaFragment
    }

    private fun setupBottomNavigation() {
        b.bottomNavigation.selectedItemId = R.id.navBeranda

        b.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navBeranda -> showFragment(berandaFragment)
                R.id.navProduk -> showFragment(produkFragment)
                R.id.navKeranjang -> showFragment(keranjangFragment)
                R.id.navPesanan -> showFragment(pesananFragment)
                R.id.navProfil -> showFragment(profilFragment)
            }
            true
        }
    }

    private fun showFragment(fragment: Fragment) {
        if (fragment == activeFragment) return

        supportFragmentManager.beginTransaction()
            .hide(activeFragment)
            .show(fragment)
            .commit()

        activeFragment = fragment
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
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
        return true
    }
}