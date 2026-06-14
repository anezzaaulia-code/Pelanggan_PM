package anezza.aulia.pelanggan_pm

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

    private var tabAktif: String = "beranda"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        session = SessionManager(this)

        setSupportActionBar(b.toolbarMain)
        supportActionBar?.title = "Si Tahu"

        rapikanSystemBar()
        setupInsetsToolbar()
        setupBottomNav()

        if (savedInstanceState == null) {
            val openTab = intent.getStringExtra("open_tab")

            when (openTab) {
                "produk" -> pilihTab(R.id.navProduk, ProdukFragment(), "produk")
                "keranjang" -> pilihTab(R.id.navKeranjang, KeranjangFragment(), "keranjang")
                "pesanan" -> pilihTab(R.id.navPesanan, PesananFragment(), "pesanan")
                "profil" -> pilihTab(R.id.navProfil, ProfilFragment(), "profil")
                else -> pilihTab(R.id.navBeranda, BerandaFragment(), "beranda")
            }
        }
    }

    private fun rapikanSystemBar() {
        window.statusBarColor = ContextCompat.getColor(this, R.color.cream_light)
        window.navigationBarColor = Color.BLACK

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility =
                window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.decorView.systemUiVisibility =
                window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
        }
    }

    private fun setupInsetsToolbar() {
        ViewCompat.setOnApplyWindowInsetsListener(b.rootMain) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val toolbarHeight = dp(56) + systemBars.top

            val toolbarParams = b.toolbarMain.layoutParams
            toolbarParams.height = toolbarHeight
            b.toolbarMain.layoutParams = toolbarParams

            b.toolbarMain.setPadding(
                dp(14),
                systemBars.top,
                dp(8),
                0
            )

            insets
        }
    }

    private fun setupBottomNav() {
        b.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navBeranda -> {
                    if (tabAktif != "beranda") {
                        bukaFragment(BerandaFragment(), "beranda")
                    }
                    true
                }

                R.id.navProduk -> {
                    if (tabAktif != "produk") {
                        bukaFragment(ProdukFragment(), "produk")
                    }
                    true
                }

                R.id.navKeranjang -> {
                    if (tabAktif != "keranjang") {
                        bukaFragment(KeranjangFragment(), "keranjang")
                    }
                    true
                }

                R.id.navPesanan -> {
                    if (tabAktif != "pesanan") {
                        bukaFragment(PesananFragment(), "pesanan")
                    }
                    true
                }

                R.id.navProfil -> {
                    if (tabAktif != "profil") {
                        bukaFragment(ProfilFragment(), "profil")
                    }
                    true
                }

                else -> false
            }
        }
    }

    private fun pilihTab(menuId: Int, fragment: Fragment, namaTab: String) {
        tabAktif = namaTab
        b.bottomNavigation.menu.findItem(menuId).isChecked = true
        bukaFragment(fragment, namaTab)
    }

    private fun bukaFragment(fragment: Fragment, namaTab: String) {
        tabAktif = namaTab

        supportFragmentManager.beginTransaction()
            .replace(R.id.frameContainer, fragment)
            .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menuRefresh -> {
                refreshTabAktif()
                Toast.makeText(this, "Halaman diperbarui", Toast.LENGTH_SHORT).show()
                true
            }

            R.id.menuLogout -> {
                session.logout()

                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun refreshTabAktif() {
        when (tabAktif) {
            "produk" -> bukaFragment(ProdukFragment(), "produk")
            "keranjang" -> bukaFragment(KeranjangFragment(), "keranjang")
            "pesanan" -> bukaFragment(PesananFragment(), "pesanan")
            "profil" -> bukaFragment(ProfilFragment(), "profil")
            else -> bukaFragment(BerandaFragment(), "beranda")
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}