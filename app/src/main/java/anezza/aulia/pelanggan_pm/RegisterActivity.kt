package anezza.aulia.pelanggan_pm

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import anezza.aulia.pelanggan_pm.databinding.ActivityRegisterBinding
import anezza.aulia.pelanggan_pm.helper.ApiConfig

class RegisterActivity : AppCompatActivity() {

    private lateinit var b: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.btnDaftar.setOnClickListener {
            register()
        }
        b.txtLogin.setOnClickListener {
            finish()
        }
    }

    private fun register() {
        val nama = b.edtNama.text.toString().trim()
        val email = b.edtEmail.text.toString().trim()
        val telepon = b.edtTelepon.text.toString().trim()
        val password = b.edtPassword.text.toString().trim()
        val konfirmasi = b.edtKonfirmasi.text.toString().trim()

        if (nama.isEmpty() || email.isEmpty() || password.isEmpty() || konfirmasi.isEmpty()) {
            Toast.makeText(this, "Data wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != konfirmasi) {
            Toast.makeText(this, "Konfirmasi password tidak sama", Toast.LENGTH_SHORT).show()
            return
        }

        val request = object : StringRequest(
            Method.POST,
            ApiConfig.REGISTER,
            { response ->
                val obj = JSONObject(response)
                Toast.makeText(this, obj.optString("message", "Registrasi berhasil"), Toast.LENGTH_SHORT).show()
                finish()
            },
            { error ->
                val msg = error.networkResponse?.data?.let {
                    JSONObject(String(it)).optString("message", "Registrasi gagal")
                } ?: "Tidak bisa terhubung ke server"
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "name" to nama,
                    "email" to email,
                    "telepon" to telepon,
                    "password" to password,
                    "password_confirmation" to konfirmasi
                )
            }
        }

        Volley.newRequestQueue(this).add(request)
    }
}