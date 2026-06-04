package anezza.aulia.pelanggan_pm

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import anezza.aulia.pelanggan_pm.databinding.ActivityUlasanBinding
import anezza.aulia.pelanggan_pm.helper.ApiConfig
import anezza.aulia.pelanggan_pm.helper.SessionManager
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class UlasanActivity : AppCompatActivity() {

    private lateinit var b: ActivityUlasanBinding
    private lateinit var session: SessionManager

    private var fotoBitmap: Bitmap? = null

    companion object {
        private const val REQ_CAMERA = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityUlasanBinding.inflate(layoutInflater)
        setContentView(b.root)

        session = SessionManager(this)

        b.btnKamera.setOnClickListener {
            bukaKamera()
        }

        b.btnKirimUlasan.setOnClickListener {
            kirimUlasan()
        }
    }

    private fun bukaKamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQ_CAMERA)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQ_CAMERA && resultCode == Activity.RESULT_OK) {
            fotoBitmap = data?.extras?.get("data") as? Bitmap
            b.imgPreview.setImageBitmap(fotoBitmap)
        }
    }

    private fun kirimUlasan() {
        val rating = b.ratingBar.rating.toInt()
        val komentar = b.edtKomentar.text.toString().trim()

        if (komentar.isEmpty()) {
            Toast.makeText(this, "Komentar ulasan wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val request = object : StringRequest(
            Request.Method.POST,
            ApiConfig.REVIEWS,
            { response ->
                val obj = JSONObject(response)
                Toast.makeText(
                    this,
                    obj.optString("message", "Ulasan berhasil dikirim"),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            },
            { error ->
                val msg = error.networkResponse?.data?.let {
                    try {
                        JSONObject(String(it)).optString("message", "Gagal kirim ulasan")
                    } catch (e: Exception) {
                        "Gagal kirim ulasan"
                    }
                } ?: "Tidak bisa terhubung ke server"

                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "rating" to rating.toString(),
                    "komentar" to komentar
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