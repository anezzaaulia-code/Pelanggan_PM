package anezza.aulia.pelanggan_pm

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
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

    private lateinit var navBeranda: LinearLayout
    private lateinit var navProduk: LinearLayout
    private lateinit var navKeranjang: LinearLayout
    private lateinit var navPesanan: LinearLayout
    private lateinit var navProfil: LinearLayout

    private lateinit var textBeranda: TextView
    private lateinit var textProduk: TextView
    private lateinit var textKeranjang: TextView
    private lateinit var textPesanan: TextView
    private lateinit var textProfil: TextView

    private lateinit var iconBeranda: TextView
    private lateinit var iconProduk: TextView
    private lateinit var iconKeranjang: TextView
    private lateinit var iconPesanan: TextView
    private lateinit var iconProfil: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = Color.parseColor("#FFF7E6")
        window.navigationBarColor = Color.parseColor("#FFFDF7")
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true

        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        session = SessionManager(this)

        initCustomNav()
        openFragment(BerandaFragment())
        setActiveNav("beranda")
    }

    private fun initCustomNav() {
        navBeranda = findViewById(R.id.navBeranda)
        navProduk = findViewById(R.id.navProduk)
        navKeranjang = findViewById(R.id.navKeranjang)
        navPesanan = findViewById(R.id.navPesanan)
        navProfil = findViewById(R.id.navProfil)

        textBeranda = findViewById(R.id.textBeranda)
        textProduk = findViewById(R.id.textProduk)
        textKeranjang = findViewById(R.id.textKeranjang)
        textPesanan = findViewById(R.id.textPesanan)
        textProfil = findViewById(R.id.textProfil)

        iconBeranda = findViewById(R.id.iconBeranda)
        iconProduk = findViewById(R.id.iconProduk)
        iconKeranjang = findViewById(R.id.iconKeranjang)
        iconPesanan = findViewById(R.id.iconPesanan)
        iconProfil = findViewById(R.id.iconProfil)

        navBeranda.setOnClickListener {
            openFragment(BerandaFragment())
            setActiveNav("beranda")
        }

        navProduk.setOnClickListener {
            openFragment(ProdukFragment())
            setActiveNav("produk")
        }

        navKeranjang.setOnClickListener {
            openFragment(KeranjangFragment())
            setActiveNav("keranjang")
        }

        navPesanan.setOnClickListener {
            openFragment(PesananFragment())
            setActiveNav("pesanan")
        }

        navProfil.setOnClickListener {
            openFragment(ProfilFragment())
            setActiveNav("profil")
        }
    }

    private fun setActiveNav(menu: String) {
        resetNav()

        when (menu) {
            "beranda" -> active(navBeranda, textBeranda, iconBeranda)
            "produk" -> active(navProduk, textProduk, iconProduk)
            "keranjang" -> active(navKeranjang, textKeranjang, iconKeranjang)
            "pesanan" -> active(navPesanan, textPesanan, iconPesanan)
            "profil" -> active(navProfil, textProfil, iconProfil)
        }
    }

    private fun resetNav() {
        val normalColor = ContextCompat.getColor(this, R.color.brand_dark)

        val navs = listOf(navBeranda, navProduk, navKeranjang, navPesanan, navProfil)
        val texts = listOf(textBeranda, textProduk, textKeranjang, textPesanan, textProfil)
        val icons = listOf(iconBeranda, iconProduk, iconKeranjang, iconPesanan, iconProfil)

        navs.forEach {
            it.background = null
        }

        texts.forEach {
            it.setTextColor(normalColor)
            it.setTypeface(null, Typeface.NORMAL)
        }

        icons.forEach {
            it.setTextColor(normalColor)
            it.setTypeface(null, Typeface.NORMAL)
        }
    }

    private fun active(nav: LinearLayout, text: TextView, icon: TextView) {
        val activeColor = ContextCompat.getColor(this, R.color.brown)

        nav.background = ContextCompat.getDrawable(this, R.drawable.bg_nav_selected)

        text.setTextColor(activeColor)
        text.setTypeface(null, Typeface.BOLD)

        icon.setTextColor(activeColor)
        icon.setTypeface(null, Typeface.BOLD)
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