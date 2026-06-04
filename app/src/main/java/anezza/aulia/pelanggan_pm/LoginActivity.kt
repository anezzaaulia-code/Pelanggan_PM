package anezza.aulia.pelanggan_pm

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import anezza.aulia.pelanggan_pm.databinding.ActivityLoginBinding
import anezza.aulia.pelanggan_pm.helper.ApiConfig
import anezza.aulia.pelanggan_pm.helper.SessionManager

class LoginActivity : AppCompatActivity() {

    private lateinit var b: ActivityLoginBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(b.root)

        session = SessionManager(this)

        b.btnLogin.setOnClickListener {
            login()
        }

        b.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun login() {
        val email = b.edtEmail.text.toString().trim()
        val password = b.edtPassword.text.toString().trim()

        if (email.isEmpty()) {
            b.edtEmail.error = "Email wajib diisi"
            b.edtEmail.requestFocus()
            return
        }

        if (password.isEmpty()) {
            b.edtPassword.error = "Password wajib diisi"
            b.edtPassword.requestFocus()
            return
        }

        Log.d("LOGIN_URL", ApiConfig.LOGIN)

        val request = object : StringRequest(
            Method.POST,
            ApiConfig.LOGIN,
            { response ->
                try {
                    Log.d("LOGIN_RESPONSE", response)

                    val obj = JSONObject(response)
                    val success = obj.optBoolean("success", false)

                    if (success) {
                        val token = obj.optString("token", "")
                        val user = obj.optJSONObject("user")

                        if (token.isEmpty() || user == null) {
                            Toast.makeText(
                                this,
                                "Response login tidak lengkap",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            val idUser = user.optInt("id", 0)
                            val namaUser = user.optString("name", "")
                            val emailUser = user.optString("email", "")
                            val teleponUser = user.optString("telepon", "")

                            session.saveLogin(
                                token = token,
                                id = idUser,
                                name = namaUser,
                                email = emailUser,
                                telepon = teleponUser
                            )

                            Toast.makeText(this, "Login berhasil", Toast.LENGTH_SHORT).show()

                            val intent = Intent(this, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        val message = obj.optString("message", "Login gagal")
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                    }

                } catch (e: Exception) {
                    Log.e("LOGIN_PARSE_ERROR", e.message.toString(), e)
                    Toast.makeText(
                        this,
                        "Error membaca response login: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            },
            { error ->
                val statusCode = error.networkResponse?.statusCode
                val responseBody = error.networkResponse?.data?.let { String(it) }

                val msg = if (responseBody != null) {
                    "Error $statusCode: $responseBody"
                } else {
                    "Tidak bisa terhubung ke server: ${error.message}"
                }

                Log.e("LOGIN_ERROR", msg)
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "email" to email,
                    "password" to password,
                    "role" to "pembeli"
                )
            }

            override fun getHeaders(): MutableMap<String, String> {
                return hashMapOf(
                    "Accept" to "application/json"
                )
            }
        }

        Volley.newRequestQueue(this).add(request)
    }
}