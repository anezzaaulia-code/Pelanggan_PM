package anezza.aulia.pelanggan_pm

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import anezza.aulia.pelanggan_pm.databinding.ActivityMainBinding
import anezza.aulia.pelanggan_pm.fragment.BerandaFragment
import anezza.aulia.pelanggan_pm.fragment.KeranjangFragment
import anezza.aulia.pelanggan_pm.fragment.PesananFragment
import anezza.aulia.pelanggan_pm.fragment.ProdukFragment
import anezza.aulia.pelanggan_pm.fragment.ProfilFragment

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding

    private val warnaAktif by lazy {
        ContextCompat.getColor(this, R.color.cream_primary_dark)
    }

    private val warnaNonAktif by lazy {
        ContextCompat.getColor(this, R.color.brown_text)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        rapikanSystemNavigationBar()
        setupBottomNav()

        val openTab = intent.getStringExtra("open_tab")

        if (savedInstanceState == null) {
            when (openTab) {
                "produk" -> bukaFragment(ProdukFragment(), "produk")
                "keranjang" -> bukaFragment(KeranjangFragment(), "keranjang")
                "pesanan" -> bukaFragment(PesananFragment(), "pesanan")
                "profil" -> bukaFragment(ProfilFragment(), "profil")
                else -> bukaFragment(BerandaFragment(), "beranda")
            }
        }
    }

    private fun rapikanSystemNavigationBar() {
        window.navigationBarColor = Color.BLACK

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.decorView.systemUiVisibility =
                window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
        }
    }

    private fun setupBottomNav() {
        b.navBeranda.setOnClickListener {
            bukaFragment(BerandaFragment(), "beranda")
        }

        b.navProduk.setOnClickListener {
            bukaFragment(ProdukFragment(), "produk")
        }

        b.navKeranjang.setOnClickListener {
            bukaFragment(KeranjangFragment(), "keranjang")
        }

        b.navPesanan.setOnClickListener {
            bukaFragment(PesananFragment(), "pesanan")
        }

        b.navProfil.setOnClickListener {
            bukaFragment(ProfilFragment(), "profil")
        }
    }

    private fun bukaFragment(fragment: Fragment, tab: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameContainer, fragment)
            .commit()

        setNavAktif(tab)
    }

    private fun setNavAktif(tab: String) {
        setItemNav(b.iconBeranda, b.txtBeranda, tab == "beranda")
        setItemNav(b.iconProduk, b.txtProduk, tab == "produk")
        setItemNav(b.iconKeranjang, b.txtKeranjang, tab == "keranjang")
        setItemNav(b.iconPesanan, b.txtPesanan, tab == "pesanan")
        setItemNav(b.iconProfil, b.txtProfil, tab == "profil")
    }

    private fun setItemNav(icon: TextView, label: TextView, aktif: Boolean) {
        val warna = if (aktif) warnaAktif else warnaNonAktif
        val alpha = if (aktif) 1f else 0.62f

        icon.setTextColor(warna)
        label.setTextColor(warna)

        icon.alpha = alpha
        label.alpha = alpha
    }
}