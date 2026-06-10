package anezza.aulia.pelanggan_pm

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import anezza.aulia.pelanggan_pm.adapter.UlasanSayaAdapter
import anezza.aulia.pelanggan_pm.databinding.ActivityUlasanSayaBinding
import anezza.aulia.pelanggan_pm.helper.ApiConfig
import anezza.aulia.pelanggan_pm.helper.SessionManager
import anezza.aulia.pelanggan_pm.model.UlasanSaya
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject

class UlasanSayaActivity : AppCompatActivity() {

    private lateinit var b: ActivityUlasanSayaBinding
    private lateinit var session: SessionManager

    private val listUlasan = ArrayList<UlasanSaya>()
    private lateinit var adapter: UlasanSayaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityUlasanSayaBinding.inflate(layoutInflater)
        setContentView(b.root)

        session = SessionManager(this)

        adapter = UlasanSayaAdapter(this, listUlasan)

        b.rvUlasanSaya.layoutManager = LinearLayoutManager(this)
        b.rvUlasanSaya.adapter = adapter

        loadUlasanSaya()
    }

    private fun loadUlasanSaya() {
        b.cardKosong.visibility = View.VISIBLE
        b.rvUlasanSaya.visibility = View.GONE
        b.txtKosongTitle.text = "Memuat ulasan..."
        b.txtKosongDesc.text = "Sebentar ya, data ulasan kamu sedang diambil."

        val request = object : StringRequest(
            Request.Method.GET,
            ApiConfig.MY_REVIEWS,
            { response ->
                try {
                    listUlasan.clear()

                    val obj = JSONObject(response)
                    val data = obj.optJSONObject("data")
                    val arr = data?.optJSONArray("data") ?: JSONArray()

                    for (i in 0 until arr.length()) {
                        val item = arr.getJSONObject(i)

                        listUlasan.add(
                            UlasanSaya(
                                id = item.optInt("id", 0),
                                pesananId = item.optInt("pesanan_id", 0),
                                nomorInvoice = item.optString("nomor_invoice", "-"),
                                produkId = item.optInt("produk_id", 0),
                                namaProduk = item.optString("nama_produk", "Produk"),
                                rating = item.optInt("rating", 0),
                                komentar = item.optString("komentar", ""),
                                fotoUlasan = item.optString("foto_ulasan", null),
                                videoUlasan = item.optString("video_ulasan", null),
                                tanggal = rapikanTanggal(item.optString("created_at", ""))
                            )
                        )
                    }

                    adapter.notifyDataSetChanged()

                    if (listUlasan.isEmpty()) {
                        b.cardKosong.visibility = View.VISIBLE
                        b.rvUlasanSaya.visibility = View.GONE
                        b.txtKosongTitle.text = "Belum ada ulasan"
                        b.txtKosongDesc.text = "Ulasan bisa diberikan setelah pesanan selesai."
                    } else {
                        b.cardKosong.visibility = View.GONE
                        b.rvUlasanSaya.visibility = View.VISIBLE
                        b.txtInfoUlasan.text = "${listUlasan.size} ulasan pernah kamu kirim."
                    }
                } catch (e: Exception) {
                    tampilError("Data ulasan gagal dibaca")
                }
            },
            { error ->
                val msg = error.networkResponse?.data?.let {
                    try {
                        JSONObject(String(it)).optString("message", "Gagal memuat ulasan")
                    } catch (e: Exception) {
                        "Gagal memuat ulasan"
                    }
                } ?: "Tidak bisa terhubung ke server"

                tampilError(msg)
            }
        ) {
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

    private fun tampilError(msg: String) {
        b.cardKosong.visibility = View.VISIBLE
        b.rvUlasanSaya.visibility = View.GONE
        b.txtKosongTitle.text = "Gagal memuat ulasan"
        b.txtKosongDesc.text = msg
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    private fun rapikanTanggal(value: String): String {
        if (value.isEmpty()) return ""
        return value
            .replace("T", " ")
            .replace(".000000Z", "")
            .take(16)
    }
}