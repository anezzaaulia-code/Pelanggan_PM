package anezza.aulia.pelanggan_pm

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import anezza.aulia.pelanggan_pm.adapter.UlasanProdukAdapter
import anezza.aulia.pelanggan_pm.databinding.ActivityDetailProdukBinding
import anezza.aulia.pelanggan_pm.helper.ApiConfig
import anezza.aulia.pelanggan_pm.helper.CartDbHelper
import anezza.aulia.pelanggan_pm.model.CartItem
import anezza.aulia.pelanggan_pm.model.UlasanProduk
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale

class DetailProdukActivity : AppCompatActivity() {

    private lateinit var b: ActivityDetailProdukBinding
    private lateinit var db: CartDbHelper

    private val listUlasan = ArrayList<UlasanProduk>()
    private lateinit var ulasanAdapter: UlasanProdukAdapter

    private var produkId = 0
    private var nama = ""
    private var harga = 0.0
    private var stok = 0
    private var satuan = ""
    private var gambar: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityDetailProdukBinding.inflate(layoutInflater)
        setContentView(b.root)

        db = CartDbHelper(this)

        produkId = intent.getIntExtra("id", 0)

        if (produkId <= 0) {
            Toast.makeText(this, "Produk tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupUlasan()
        loadDetail()
        loadUlasanProduk()

        b.btnTambahKeranjang.setOnClickListener {
            tambahKeranjang(false)
        }

        b.btnBeliSekarang.setOnClickListener {
            tambahKeranjang(true)
        }
    }

    private fun setupUlasan() {
        ulasanAdapter = UlasanProdukAdapter(this, listUlasan)

        b.rvUlasanProduk.layoutManager = LinearLayoutManager(this)
        b.rvUlasanProduk.adapter = ulasanAdapter
        b.rvUlasanProduk.isNestedScrollingEnabled = false
    }

    private fun loadDetail() {
        val request = StringRequest(
            Request.Method.GET,
            ApiConfig.PRODUCTS + "/$produkId",
            { response ->
                try {
                    val obj = JSONObject(response)
                    val data = obj.optJSONObject("data")

                    if (data == null) {
                        Toast.makeText(this, "Data produk tidak ditemukan", Toast.LENGTH_SHORT).show()
                        return@StringRequest
                    }

                    nama = data.optString("nama", "-")
                    harga = data.optDouble("harga", 0.0)
                    stok = data.optInt("stok", 0)
                    satuan = data.optString("satuan", "")
                    gambar = data.optString("gambar_utama", "")

                    b.txtNama.text = nama
                    b.txtHarga.text = formatRupiah(harga)
                    b.txtStok.text = "Stok: $stok $satuan"
                    b.txtIsi.text = "Isi per satuan: ${data.optInt("isi_per_satuan", 0)}"
                    b.txtBerat.text = "Berat: ${data.optDouble("berat", 0.0)}"
                    b.txtMasaSimpan.text = "Masa simpan: ${data.optInt("masa_simpan", 0)} hari"

                    b.txtDeskripsi.text = data.optString("deskripsi", "-")
                    b.txtSaranPenyimpanan.text = data.optString("saran_penyimpanan", "-")
                    b.txtSaranPenyajian.text = data.optString("saran_penyajian", "-")

                    tampilkanGambarProduk(gambar)
                } catch (e: Exception) {
                    Toast.makeText(this, "Data produk gagal dibaca", Toast.LENGTH_SHORT).show()
                }
            },
            {
                Toast.makeText(this, "Gagal mengambil detail produk", Toast.LENGTH_SHORT).show()
            }
        )

        Volley.newRequestQueue(this).add(request)
    }

    private fun loadUlasanProduk() {
        b.txtJumlahUlasan.text = "Memuat ulasan..."
        b.txtUlasanKosong.visibility = View.VISIBLE
        b.rvUlasanProduk.visibility = View.GONE

        val request = StringRequest(
            Request.Method.GET,
            ApiConfig.PRODUCTS + "/$produkId/reviews",
            { response ->
                try {
                    listUlasan.clear()

                    val obj = JSONObject(response)
                    val data = obj.optJSONObject("data")
                    val arr = data?.optJSONArray("data") ?: JSONArray()

                    var totalRating = 0

                    for (i in 0 until arr.length()) {
                        val item = arr.getJSONObject(i)

                        val rating = item.optInt("rating", 0)
                        totalRating += rating

                        listUlasan.add(
                            UlasanProduk(
                                id = item.optInt("id", 0),
                                user = item.optString("user", "Pembeli"),
                                rating = rating,
                                komentar = item.optString("komentar", ""),
                                fotoUlasan = item.optString("foto_ulasan", null),
                                videoUlasan = item.optString("video_ulasan", null),
                                tanggal = rapikanTanggal(item.optString("created_at", ""))
                            )
                        )
                    }

                    ulasanAdapter.notifyDataSetChanged()

                    if (listUlasan.isEmpty()) {
                        b.txtJumlahUlasan.text = "Belum ada ulasan"
                        b.txtRingkasanUlasan.text = "⭐ Belum ada ulasan"
                        b.txtUlasanKosong.visibility = View.VISIBLE
                        b.rvUlasanProduk.visibility = View.GONE
                    } else {
                        val rata = totalRating.toDouble() / listUlasan.size.toDouble()
                        val rataText = String.format(Locale("id", "ID"), "%.1f", rata)

                        b.txtJumlahUlasan.text = "${listUlasan.size} ulasan pembeli"
                        b.txtRingkasanUlasan.text = "⭐ $rataText • ${listUlasan.size} ulasan"
                        b.txtUlasanKosong.visibility = View.GONE
                        b.rvUlasanProduk.visibility = View.VISIBLE
                    }
                } catch (e: Exception) {
                    b.txtJumlahUlasan.text = "Ulasan gagal dibaca"
                    b.txtRingkasanUlasan.text = "⭐ Ulasan belum tersedia"
                    b.txtUlasanKosong.visibility = View.VISIBLE
                    b.rvUlasanProduk.visibility = View.GONE
                }
            },
            {
                b.txtJumlahUlasan.text = "Gagal memuat ulasan"
                b.txtRingkasanUlasan.text = "⭐ Ulasan belum tersedia"
                b.txtUlasanKosong.visibility = View.VISIBLE
                b.rvUlasanProduk.visibility = View.GONE
            }
        )

        Volley.newRequestQueue(this).add(request)
    }

    private fun tampilkanGambarProduk(urlGambar: String?) {
        if (!urlGambar.isNullOrEmpty()) {
            b.imgProduk.visibility = View.VISIBLE
            b.txtIconDefault.visibility = View.GONE

            Picasso.get()
                .load(urlGambar)
                .placeholder(R.color.hijau_muda)
                .error(R.color.hijau_muda)
                .into(b.imgProduk)
        } else {
            b.imgProduk.visibility = View.GONE
            b.txtIconDefault.visibility = View.VISIBLE
        }
    }

    private fun tambahKeranjang(lanjutCheckout: Boolean) {
        val jumlah = b.edtJumlah.text.toString().toIntOrNull() ?: 1

        if (nama.isEmpty()) {
            Toast.makeText(this, "Detail produk belum dimuat", Toast.LENGTH_SHORT).show()
            return
        }

        if (jumlah < 1) {
            Toast.makeText(this, "Jumlah tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        if (stok <= 0) {
            Toast.makeText(this, "Stok produk sedang kosong", Toast.LENGTH_SHORT).show()
            return
        }

        if (jumlah > stok) {
            Toast.makeText(this, "Jumlah melebihi stok", Toast.LENGTH_SHORT).show()
            return
        }

        db.addToCart(
            CartItem(
                produkId = produkId,
                nama = nama,
                harga = harga,
                stok = stok,
                satuan = satuan,
                gambar = gambar,
                jumlah = jumlah
            )
        )

        Toast.makeText(this, "Produk masuk keranjang", Toast.LENGTH_SHORT).show()

        if (lanjutCheckout) {
            startActivity(Intent(this, CheckoutActivity::class.java))
        }
    }

    private fun rapikanTanggal(value: String): String {
        if (value.isEmpty()) return ""
        return value
            .replace("T", " ")
            .replace(".000000Z", "")
            .take(16)
    }

    private fun formatRupiah(value: Double): String {
        return NumberFormat
            .getCurrencyInstance(Locale("id", "ID"))
            .format(value)
            .replace(",00", "")
    }
}