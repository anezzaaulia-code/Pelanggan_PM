package anezza.aulia.pelanggan_pm

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import anezza.aulia.pelanggan_pm.databinding.ActivityCheckoutBinding
import anezza.aulia.pelanggan_pm.helper.ApiConfig
import anezza.aulia.pelanggan_pm.helper.CartDbHelper
import anezza.aulia.pelanggan_pm.helper.SessionManager
import anezza.aulia.pelanggan_pm.model.Alamat
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CheckoutActivity : AppCompatActivity() {

    private lateinit var b: ActivityCheckoutBinding
    private lateinit var db: CartDbHelper
    private lateinit var session: SessionManager

    private val listAlamat = ArrayList<Alamat>()
    private val kalender = Calendar.getInstance()

    private var tanggalPesananValue = ""
    private var jamPesananValue = ""

    private val metodeBayarLabel = arrayListOf(
        "COD / Bayar di Tempat",
        "Transfer Bank"
    )

    private val metodeBayarValue = arrayListOf(
        "cod",
        "transfer_bank"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(b.root)

        db = CartDbHelper(this)
        session = SessionManager(this)

        setupSpinnerPembayaran()
        setupMetodePengambilan()
        setupJadwalPesanan()
        tampilRingkasan()
        loadAlamat()

        b.btnBuatPesanan.setOnClickListener {
            buatPesanan()
        }
    }

    override fun onResume() {
        super.onResume()
        loadAlamat()
    }

    private fun setupSpinnerPembayaran() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            metodeBayarLabel
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        b.spMetodeBayar.adapter = adapter

        b.spMetodeBayar.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val metode = metodeBayarValue.getOrElse(position) { "cod" }
                tampilInfoPembayaran(metode)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    private fun tampilInfoPembayaran(metode: String) {
        when (metode) {
            "transfer_bank" -> {
                b.txtInfoPembayaran.text =
                    "Transfer bank akan diverifikasi admin setelah pembeli mengunggah bukti pembayaran. QR Code bukan QRIS, tetapi digunakan nanti untuk validasi pengambilan pesanan."
            }

            else -> {
                b.txtInfoPembayaran.text =
                    "COD / tunai dibayar saat pesanan diterima atau diambil di toko. QR Code akan digunakan untuk validasi pengambilan pesanan."
            }
        }
    }

    private fun setupJadwalPesanan() {
        val formatTanggalTampil = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        val formatTanggalValue = SimpleDateFormat("yyyy-MM-dd", Locale("id", "ID"))
        val formatJamValue = SimpleDateFormat("HH:mm", Locale("id", "ID"))

        tanggalPesananValue = formatTanggalValue.format(kalender.time)
        jamPesananValue = formatJamValue.format(kalender.time)

        b.txtTanggalPesanan.text = formatTanggalTampil.format(kalender.time)
        b.txtJamPesanan.text = jamPesananValue

        b.btnPilihTanggal.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    kalender.set(Calendar.YEAR, year)
                    kalender.set(Calendar.MONTH, month)
                    kalender.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    tanggalPesananValue = formatTanggalValue.format(kalender.time)
                    b.txtTanggalPesanan.text = formatTanggalTampil.format(kalender.time)

                    tampilRingkasan()
                },
                kalender.get(Calendar.YEAR),
                kalender.get(Calendar.MONTH),
                kalender.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        b.btnPilihJam.setOnClickListener {
            TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    kalender.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    kalender.set(Calendar.MINUTE, minute)

                    jamPesananValue = formatJamValue.format(kalender.time)
                    b.txtJamPesanan.text = jamPesananValue

                    tampilRingkasan()
                },
                kalender.get(Calendar.HOUR_OF_DAY),
                kalender.get(Calendar.MINUTE),
                true
            ).show()
        }
    }

    private fun setupMetodePengambilan() {
        b.rbAmbil.isChecked = true
        b.spAlamat.visibility = View.GONE
        b.btnTambahAlamat.visibility = View.GONE
        b.txtAlamatInfo.text = "Alamat toko akan digunakan untuk metode Ambil di Toko."

        b.rgMetode.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == b.rbKurir.id) {
                b.txtAlamatInfo.text =
                    "Pilih alamat pengiriman untuk Kurir Toko. Ongkir mengikuti pengaturan toko."
                b.spAlamat.visibility = View.VISIBLE
                b.btnTambahAlamat.visibility = View.VISIBLE
            } else {
                b.txtAlamatInfo.text =
                    "Alamat toko akan digunakan untuk metode Ambil di Toko."
                b.spAlamat.visibility = View.GONE
                b.btnTambahAlamat.visibility = View.GONE
            }

            tampilRingkasan()
        }

        b.btnTambahAlamat.setOnClickListener {
            startActivity(Intent(this, AlamatActivity::class.java))
        }
    }

    private fun tampilRingkasan() {
        val cart = db.getCart()
        val subtotal = db.getTotal()

        if (cart.isEmpty()) {
            b.txtInfoKeranjang.text = "Keranjang kosong"
            b.txtRingkasan.text = "Belum ada produk yang dipilih."
            b.txtTotal.text = formatRupiah(0.0)
            return
        }

        val ringkasan = StringBuilder()

        cart.forEach {
            ringkasan.append("${it.nama} x ${it.jumlah} = ${formatRupiah(it.harga * it.jumlah)}\n")
        }

        ringkasan.append("\nSubtotal produk: ${formatRupiah(subtotal)}")

        if (b.rbKurir.isChecked) {
            ringkasan.append("\nOngkir: dihitung sistem sesuai pengaturan toko")
        } else {
            ringkasan.append("\nOngkir: ${formatRupiah(0.0)}")
        }

        if (tanggalPesananValue.isNotBlank() && jamPesananValue.isNotBlank()) {
            ringkasan.append("\nJadwal: ${b.txtTanggalPesanan.text}, $jamPesananValue")
        }

        b.txtInfoKeranjang.text = "${cart.size} produk siap di-checkout"
        b.txtRingkasan.text = ringkasan.toString()
        b.txtTotal.text = formatRupiah(subtotal)
    }

    private fun loadAlamat() {
        val request = object : StringRequest(
            Request.Method.GET,
            ApiConfig.ADDRESSES,
            { response ->
                listAlamat.clear()

                val obj = JSONObject(response)
                val arr = obj.optJSONArray("data") ?: JSONArray()
                val namaAlamat = ArrayList<String>()

                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)

                    val alamat = Alamat(
                        id = o.optInt("id", 0),
                        namaPenerima = o.optString("nama_penerima", ""),
                        telepon = o.optString("telepon", ""),
                        alamatLengkap = o.optString("alamat_lengkap", ""),
                        latitude = if (o.isNull("latitude")) null else o.optDouble("latitude"),
                        longitude = if (o.isNull("longitude")) null else o.optDouble("longitude"),
                        utama = o.optBoolean("utama", false)
                    )

                    listAlamat.add(alamat)

                    val labelUtama = if (alamat.utama) " - Utama" else ""
                    namaAlamat.add("${alamat.namaPenerima}$labelUtama | ${alamat.alamatLengkap}")
                }

                if (namaAlamat.isEmpty()) {
                    namaAlamat.add("Belum ada alamat")
                }

                b.spAlamat.adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_dropdown_item,
                    namaAlamat
                )
            },
            {
                // Ambil di toko tetap bisa jalan walaupun alamat gagal dimuat.
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

    private fun buatPesanan() {
        val cart = db.getCart()

        if (cart.isEmpty()) {
            Toast.makeText(this, "Keranjang masih kosong", Toast.LENGTH_SHORT).show()
            return
        }

        if (!b.cbSetuju.isChecked) {
            Toast.makeText(this, "Centang persetujuan dulu", Toast.LENGTH_SHORT).show()
            return
        }

        val metodePengambilan = if (b.rbAmbil.isChecked) {
            "ambil_toko"
        } else {
            "kurir_toko"
        }

        val metodePembayaran = metodeBayarValue[b.spMetodeBayar.selectedItemPosition]

        var alamatId: Int? = null

        if (metodePengambilan == "kurir_toko") {
            if (listAlamat.isEmpty()) {
                Toast.makeText(
                    this,
                    "Tambahkan alamat dulu untuk Kurir Toko",
                    Toast.LENGTH_LONG
                ).show()
                return
            }

            val posisiAlamat = b.spAlamat.selectedItemPosition

            if (posisiAlamat < 0 || posisiAlamat >= listAlamat.size) {
                Toast.makeText(
                    this,
                    "Pilih alamat pengiriman dulu",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            alamatId = listAlamat[posisiAlamat].id
        }

        val items = JSONArray()

        cart.forEach {
            val row = JSONObject()
            row.put("produk_id", it.produkId)
            row.put("jumlah", it.jumlah)
            items.put(row)
        }

        val body = JSONObject()
        body.put("items", items)
        body.put("metode_pengambilan", metodePengambilan)
        body.put("metode_pembayaran", metodePembayaran)
        body.put("tanggal_pesanan_pilihan", tanggalPesananValue)
        body.put("jam_pesanan_pilihan", jamPesananValue)

        if (alamatId != null) {
            body.put("alamat_pengiriman_id", alamatId)
        }

        b.btnBuatPesanan.isEnabled = false
        b.btnBuatPesanan.text = "Memproses..."

        val request = object : JsonObjectRequest(
            Request.Method.POST,
            ApiConfig.ORDERS,
            body,
            { response ->
                Toast.makeText(
                    this,
                    response.optString("message", "Pesanan berhasil dibuat"),
                    Toast.LENGTH_SHORT
                ).show()

                db.clearCart()

                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                intent.putExtra("open_tab", "pesanan")
                startActivity(intent)
                finish()
            },
            { error ->
                b.btnBuatPesanan.isEnabled = true
                b.btnBuatPesanan.text = "Buat Pesanan"

                val msg = error.networkResponse?.data?.let {
                    try {
                        JSONObject(String(it)).optString("message", "Checkout gagal")
                    } catch (e: Exception) {
                        "Checkout gagal"
                    }
                } ?: "Tidak bisa terhubung ke server"

                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
            }
        ) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): MutableMap<String, String> {
                return hashMapOf(
                    "Authorization" to "Bearer ${session.token()}",
                    "Accept" to "application/json",
                    "Content-Type" to "application/json"
                )
            }
        }

        Volley.newRequestQueue(this).add(request)
    }

    private fun formatRupiah(value: Double): String {
        return NumberFormat
            .getCurrencyInstance(Locale("id", "ID"))
            .format(value)
            .replace(",00", "")
    }
}