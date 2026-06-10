package anezza.aulia.pelanggan_pm

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import anezza.aulia.pelanggan_pm.databinding.ActivityUlasanBinding
import anezza.aulia.pelanggan_pm.helper.ApiConfig
import anezza.aulia.pelanggan_pm.helper.SessionManager
import anezza.aulia.pelanggan_pm.helper.VolleyMultipartRequest
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.toolbox.Volley
import com.github.dhaval2404.imagepicker.ImagePicker
import com.permissionx.guolindev.PermissionX
import org.json.JSONObject

class UlasanActivity : AppCompatActivity() {

    private lateinit var b: ActivityUlasanBinding
    private lateinit var session: SessionManager

    private var fotoUri: Uri? = null

    private var pesananId: Int = 0
    private var produkId: Int = 0
    private var namaProduk: String = ""

    companion object {
        private const val REQ_IMAGE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityUlasanBinding.inflate(layoutInflater)
        setContentView(b.root)

        session = SessionManager(this)

        pesananId = intent.getIntExtra("pesanan_id", 0)
        produkId = intent.getIntExtra("produk_id", 0)
        namaProduk = intent.getStringExtra("nama_produk") ?: ""

        b.emptyPreview.visibility = View.VISIBLE

        if (namaProduk.isNotEmpty()) {
            b.txtInfoProdukUlasan.text = "Produk: $namaProduk"
        } else {
            b.txtInfoProdukUlasan.text =
                "Ulasan akan membantu pembeli lain memilih produk MAR Tahu."
        }

        b.btnKamera.setOnClickListener {
            tampilMenuFoto()
        }

        b.btnKirimUlasan.setOnClickListener {
            kirimUlasan()
        }
    }

    private fun tampilMenuFoto() {
        val popup = PopupMenu(this, b.btnKamera)
        popup.menu.add(0, 1, 1, "Ambil dari Kamera")
        popup.menu.add(0, 2, 2, "Pilih dari Galeri")

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> {
                    mintaIzinKamera()
                    true
                }

                2 -> {
                    bukaGaleri()
                    true
                }

                else -> false
            }
        }

        popup.show()
    }

    private fun mintaIzinKamera() {
        PermissionX.init(this)
            .permissions(android.Manifest.permission.CAMERA)
            .request { allGranted, _, _ ->
                if (allGranted) {
                    bukaKamera()
                } else {
                    Toast.makeText(
                        this,
                        "Izin kamera belum diberikan",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun bukaKamera() {
        ImagePicker.with(this)
            .cameraOnly()
            .compress(1024)
            .maxResultSize(1080, 1080)
            .start(REQ_IMAGE)
    }

    private fun bukaGaleri() {
        ImagePicker.with(this)
            .galleryOnly()
            .compress(1024)
            .maxResultSize(1080, 1080)
            .start(REQ_IMAGE)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQ_IMAGE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    fotoUri = data?.data

                    if (fotoUri != null) {
                        b.imgPreview.setImageURI(fotoUri)
                        b.emptyPreview.visibility = View.GONE
                    } else {
                        Toast.makeText(this, "Foto tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                }

                ImagePicker.RESULT_ERROR -> {
                    Toast.makeText(
                        this,
                        ImagePicker.getError(data),
                        Toast.LENGTH_LONG
                    ).show()
                }

                else -> {
                    Toast.makeText(this, "Pemilihan foto dibatalkan", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun kirimUlasan() {
        val rating = b.ratingBar.rating.toInt()
        val komentar = b.edtKomentar.text.toString().trim()

        if (rating <= 0) {
            Toast.makeText(this, "Pilih rating dulu", Toast.LENGTH_SHORT).show()
            return
        }

        if (komentar.isEmpty()) {
            Toast.makeText(this, "Komentar ulasan wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        if (pesananId <= 0 || produkId <= 0) {
            Toast.makeText(
                this,
                "Data pesanan atau produk tidak lengkap. Buka ulasan dari pesanan selesai.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        b.btnKirimUlasan.isEnabled = false
        b.btnKirimUlasan.text = "Mengirim..."

        val request = object : VolleyMultipartRequest(
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
                b.btnKirimUlasan.isEnabled = true
                b.btnKirimUlasan.text = "Kirim Ulasan"

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
                    "pesanan_id" to pesananId.toString(),
                    "produk_id" to produkId.toString(),
                    "rating" to rating.toString(),
                    "komentar" to komentar
                )
            }

            override fun getByteData(): MutableMap<String, DataPart> {
                val dataFile = hashMapOf<String, DataPart>()

                fotoUri?.let { uri ->
                    val bytes = uriToByteArray(uri)

                    if (bytes != null) {
                        dataFile["foto_ulasan"] = DataPart(
                            fileName = "foto_ulasan_${System.currentTimeMillis()}.${getExtension(uri)}",
                            content = bytes,
                            type = contentResolver.getType(uri) ?: "image/jpeg"
                        )
                    }
                }

                return dataFile
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

    private fun uriToByteArray(uri: Uri): ByteArray? {
        return try {
            contentResolver.openInputStream(uri)?.use { input ->
                input.readBytes()
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun getExtension(uri: Uri): String {
        val mimeType = contentResolver.getType(uri)
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(mimeType)
            ?: "jpg"
    }
}