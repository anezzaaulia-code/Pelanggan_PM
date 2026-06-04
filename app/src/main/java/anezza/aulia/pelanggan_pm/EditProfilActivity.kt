package anezza.aulia.pelanggan_pm

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import anezza.aulia.pelanggan_pm.databinding.ActivityEditProfilBinding
import anezza.aulia.pelanggan_pm.helper.ApiConfig
import anezza.aulia.pelanggan_pm.helper.SessionManager
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class EditProfilActivity : AppCompatActivity() {

    private lateinit var b: ActivityEditProfilBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityEditProfilBinding.inflate(layoutInflater)
        setContentView(b.root)

        session = SessionManager(this)

        b.edtNama.setText(session.name())
        b.edtEmail.setText(session.email())
        b.edtTelepon.setText(session.telepon())

        b.btnSimpanProfil.setOnClickListener {
            updateProfil()
        }
    }

    private fun updateProfil() {
        val nama = b.edtNama.text.toString().trim()
        val email = b.edtEmail.text.toString().trim()
        val telepon = b.edtTelepon.text.toString().trim()

        if (nama.isEmpty() || email.isEmpty() || telepon.isEmpty()) {
            Toast.makeText(this, "Semua data wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val request = object : StringRequest(
            Request.Method.PUT,
            ApiConfig.PROFILE,
            { response ->
                val obj = JSONObject(response)
                Toast.makeText(
                    this,
                    obj.optString("message", "Profil berhasil diperbarui"),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            },
            { error ->
                val msg = error.networkResponse?.data?.let {
                    try {
                        JSONObject(String(it)).optString("message", "Gagal update profil")
                    } catch (e: Exception) {
                        "Gagal update profil"
                    }
                } ?: "Tidak bisa terhubung ke server"

                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "name" to nama,
                    "email" to email,
                    "telepon" to telepon
                )
            }

            @Throws(AuthFailureError::class)
            override fun getHeaders(): MutableMap<String, String> {
                return hashMapOf(
                    "Authorization" to "Bearer ${session.token()}",
                    "Accept" to "application/json"
                )
            }
        }

        Volley.newRequestQueue(this).add(request)
    }
}