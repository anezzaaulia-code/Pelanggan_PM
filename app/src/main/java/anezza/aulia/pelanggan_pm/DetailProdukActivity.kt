package anezza.aulia.pelanggan_pm

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import anezza.aulia.pelanggan_pm.databinding.ActivityDetailProdukBinding
import anezza.aulia.pelanggan_pm.helper.ApiConfig
import anezza.aulia.pelanggan_pm.helper.CartDbHelper
import anezza.aulia.pelanggan_pm.model.CartItem
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale

class DetailProdukActivity : AppCompatActivity() {

    private lateinit var b: ActivityDetailProdukBinding
    private lateinit var db: CartDbHelper

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

        loadDetail()

        b.btnTambahKeranjang.setOnClickListener {
            tambahKeranjang(false)
        }

        b.btnBeliSekarang.setOnClickListener {
            tambahKeranjang(true)
        }
    }

    private fun loadDetail() {
        val request = StringRequest(
            ApiConfig.PRODUCTS + "/$produkId",
            { response ->
                val obj = JSONObject(response)
                val data = obj.getJSONObject("data")

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

                if (!gambar.isNullOrEmpty()) {
                    Picasso.get()
                        .load(gambar)
                        .placeholder(R.color.hijau_muda)
                        .error(R.color.hijau_muda)
                        .into(b.imgProduk)
                } else {
                    b.imgProduk.setImageResource(0)
                    b.imgProduk.setBackgroundResource(R.color.hijau_muda)
                }
            },
            {
                Toast.makeText(this, "Gagal mengambil detail produk", Toast.LENGTH_SHORT).show()
            }
        )

        Volley.newRequestQueue(this).add(request)
    }

    private fun tambahKeranjang(lanjutCheckout: Boolean) {
        val jumlah = b.edtJumlah.text.toString().toIntOrNull() ?: 1

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

    private fun formatRupiah(value: Double): String {
        return NumberFormat
            .getCurrencyInstance(Locale("id", "ID"))
            .format(value)
            .replace(",00", "")
    }
}