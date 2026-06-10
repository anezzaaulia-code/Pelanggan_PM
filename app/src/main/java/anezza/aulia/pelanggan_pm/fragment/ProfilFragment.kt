package anezza.aulia.pelanggan_pm.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import anezza.aulia.pelanggan_pm.AlamatActivity
import anezza.aulia.pelanggan_pm.EditProfilActivity
import anezza.aulia.pelanggan_pm.LoginActivity
import anezza.aulia.pelanggan_pm.R
import anezza.aulia.pelanggan_pm.UlasanSayaActivity
import anezza.aulia.pelanggan_pm.VideoTokoActivity
import anezza.aulia.pelanggan_pm.databinding.FragmentProfilBinding
import anezza.aulia.pelanggan_pm.helper.SessionManager
import anezza.aulia.pelanggan_pm.helper.ThemeHelper

class ProfilFragment : Fragment() {

    private var _b: FragmentProfilBinding? = null
    private val b get() = _b!!

    private lateinit var session: SessionManager
    private lateinit var txtTemaAktif: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _b = FragmentProfilBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onResume() {
        super.onResume()
        if (::session.isInitialized) {
            tampilProfil()
            tampilTemaAktif()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        session = SessionManager(requireContext())
        tampilProfil()

        val menuEditProfil = view.findViewById<MaterialCardView>(R.id.menuEditProfil)
        val menuAlamat = view.findViewById<MaterialCardView>(R.id.menuAlamat)
        val menuUlasan = view.findViewById<MaterialCardView>(R.id.menuUlasan)
        val menuVideo = view.findViewById<MaterialCardView>(R.id.menuVideo)
        val menuTema = view.findViewById<MaterialCardView>(R.id.menuTema)
        val menuLogout = view.findViewById<MaterialCardView>(R.id.menuLogout)

        txtTemaAktif = view.findViewById(R.id.txtTemaAktif)
        tampilTemaAktif()

        menuEditProfil.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfilActivity::class.java))
        }

        menuAlamat.setOnClickListener {
            startActivity(Intent(requireContext(), AlamatActivity::class.java))
        }

        menuUlasan.setOnClickListener {
            startActivity(Intent(requireContext(), UlasanSayaActivity::class.java))
        }

        menuVideo.setOnClickListener {
            startActivity(Intent(requireContext(), VideoTokoActivity::class.java))
        }

        menuTema.setOnClickListener {
            tampilDialogTema()
        }

        menuLogout.setOnClickListener {
            session.logout()

            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

            requireActivity().finish()
        }
    }

    private fun tampilProfil() {
        val nama = session.name()
        val email = session.email()
        val telepon = session.telepon()

        b.txtNamaProfil.text = nama.ifEmpty { "Pembeli" }
        b.txtEmailProfil.text = email.ifEmpty { "-" }
        b.txtNoHpProfil.text = telepon.ifEmpty { "-" }
        b.txtInisialProfil.text = nama.firstOrNull()?.uppercase() ?: "P"
    }

    private fun tampilTemaAktif() {
        if (!::txtTemaAktif.isInitialized) return

        txtTemaAktif.text = when (ThemeHelper.getTheme(requireContext())) {
            ThemeHelper.THEME_LIGHT -> "Light Mode"
            ThemeHelper.THEME_DARK -> "Dark Mode"
            ThemeHelper.THEME_SYSTEM -> "Ikuti Sistem"
            else -> "Light Mode"
        }
    }

    private fun tampilDialogTema() {
        val pilihan = arrayOf("Light Mode", "Dark Mode", "Ikuti Sistem")

        val checkedItem = when (ThemeHelper.getTheme(requireContext())) {
            ThemeHelper.THEME_LIGHT -> 0
            ThemeHelper.THEME_DARK -> 1
            ThemeHelper.THEME_SYSTEM -> 2
            else -> 0
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Pilih Tema Aplikasi")
            .setSingleChoiceItems(pilihan, checkedItem) { dialog, which ->
                val theme = when (which) {
                    0 -> ThemeHelper.THEME_LIGHT
                    1 -> ThemeHelper.THEME_DARK
                    else -> ThemeHelper.THEME_SYSTEM
                }

                ThemeHelper.saveTheme(requireContext(), theme)
                tampilTemaAktif()
                dialog.dismiss()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}