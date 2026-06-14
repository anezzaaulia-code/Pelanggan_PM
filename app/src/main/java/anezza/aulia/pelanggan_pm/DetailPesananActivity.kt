package anezza.aulia.pelanggan_pm

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import anezza.aulia.pelanggan_pm.databinding.ActivityDetailPesananBinding
import org.json.JSONArray
import java.text.NumberFormat
import java.util.Locale

class DetailPesananActivity : AppCompatActivity() {

    private lateinit var b: ActivityDetailPesananBinding

    private var invoice: String = "-"
    private var tanggal: String = "-"
    private var total: Double = 0.0
    private var statusPesanan: String = "-"
    private var statusPembayaran: String = "-"
    private var metodePengambilan: String = "-"
    private var metodePembayaran: String = "-"
    private var qrCode: String = ""
    private var itemsJson: String = "[]"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityDetailPesananBinding.inflate(layoutInflater)
        setContentView(b.root)

        ambilDataIntent()
        tampilkanData()
        setupAksi()
    }

    private fun ambilDataIntent() {
        invoice = intent.getStringExtra("invoice") ?: "-"
        tanggal = intent.getStringExtra("tanggal") ?: "-"
        total = intent.getDoubleExtra("total", 0.0)
        statusPesanan = intent.getStringExtra("status_pesanan") ?: "-"
        statusPembayaran = intent.getStringExtra("status_pembayaran") ?: "-"
        metodePengambilan = intent.getStringExtra("metode_pengambilan") ?: "-"
        metodePembayaran = intent.getStringExtra("metode_pembayaran") ?: "-"
        qrCode = intent.getStringExtra("qr_code") ?: ""
        itemsJson = intent.getStringExtra("items_json") ?: "[]"
    }

    private fun tampilkanData() {
        b.txtInvoice.text = invoice
        b.txtTanggal.text = "Tanggal pesanan: $tanggal"

        b.txtStatusPesanan.text = "Pesanan\n${labelStatus(statusPesanan)}"
        b.txtStatusPembayaran.text = "Pembayaran\n${labelStatus(statusPembayaran)}"

        b.txtMetodeAmbil.text = "Metode pengambilan: ${labelMetodeAmbil(metodePengambilan)}"
        b.txtMetodeBayar.text = "Metode pembayaran: ${labelMetodeBayar(metodePembayaran)}"
        b.txtTotal.text = "Total ${formatRupiah(total)}"

        b.txtCatatan.text = buatCatatanStatus()

        tampilkanProduk()

        if (bisaTampilQr()) {
            b.btnQrPesanan.visibility = View.VISIBLE
        } else {
            b.btnQrPesanan.visibility = View.GONE
        }
    }

    private fun tampilkanProduk() {
        b.layoutProduk.removeAllViews()

        val arr = try {
            JSONArray(itemsJson)
        } catch (e: Exception) {
            JSONArray()
        }

        if (arr.length() == 0) {
            val kosong = TextView(this)
            kosong.text = "Detail produk belum tersedia."
            kosong.setTextColor(getColorCompat(R.color.text_gray))
            kosong.textSize = 14f
            kosong.setPadding(0, 8, 0, 8)
            b.layoutProduk.addView(kosong)
            return
        }

        for (i in 0 until arr.length()) {
            val item = arr.getJSONObject(i)

            val namaProduk = item.optString("nama_produk", "Produk")
            val jumlah = item.optInt("jumlah", 0)
            val hargaSatuan = item.optDouble("harga_satuan", 0.0)
            val subtotal = item.optDouble("subtotal", 0.0)

            val row = TextView(this)
            row.text =
                "$namaProduk\n" +
                        "$jumlah x ${formatRupiah(hargaSatuan)}\n" +
                        "Subtotal: ${formatRupiah(subtotal)}"

            row.setTextColor(getColorCompat(R.color.text_dark))
            row.textSize = 14f
            row.setPadding(dp(14), dp(12), dp(14), dp(12))
            row.setBackgroundResource(R.drawable.bg_note_box)

            val params = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            )

            params.setMargins(0, 0, 0, dp(10))
            row.layoutParams = params

            b.layoutProduk.addView(row)
        }
    }

    private fun setupAksi() {
        b.btnBack.setOnClickListener {
            finish()
        }

        b.btnQrPesanan.setOnClickListener {
            val intent = Intent(this, PesananQrActivity::class.java)
            intent.putExtra("invoice", invoice)
            intent.putExtra("qr_code", qrCode)
            intent.putExtra("status_pesanan", statusPesanan)
            intent.putExtra("status_pembayaran", statusPembayaran)
            intent.putExtra("total", formatRupiah(total))
            startActivity(intent)
        }
    }

    private fun bisaTampilQr(): Boolean {
        return metodePengambilan == "ambil_toko"
                && statusPesanan == "siap_diambil"
                && qrCode.isNotBlank()
    }

    private fun buatCatatanStatus(): String {
        return when {
            statusPesanan == "menunggu_pembayaran" -> {
                "Pesanan sudah dibuat. Silakan lakukan pembayaran sesuai metode yang dipilih."
            }

            statusPesanan == "menunggu_verifikasi" -> {
                "Bukti pembayaran sedang menunggu verifikasi admin."
            }

            statusPesanan == "diproses" || statusPesanan == "disiapkan" -> {
                "Pesanan sedang diproses oleh toko. Tunggu sampai status berubah menjadi siap diambil atau dalam pengantaran."
            }

            statusPesanan == "siap_diambil" && metodePengambilan == "ambil_toko" -> {
                if (statusPembayaran == "dibayar") {
                    "Pesanan siap diambil. Tunjukkan QR Code kepada admin toko untuk validasi pengambilan."
                } else {
                    "Pesanan siap diambil. Pembayaran belum tercatat lunas. Admin akan mengecek status pembayaran saat QR discan."
                }
            }

            statusPesanan == "dalam_pengantaran" -> {
                "Pesanan sedang dalam pengantaran. Jika sudah diterima, lakukan konfirmasi diterima dari halaman pesanan."
            }

            statusPesanan == "selesai" -> {
                "Pesanan sudah selesai. Kamu dapat memberikan ulasan untuk produk yang sudah dibeli."
            }

            statusPesanan == "dibatalkan" -> {
                "Pesanan ini sudah dibatalkan."
            }

            else -> {
                "Pantau status pesanan secara berkala dari halaman Pesanan."
            }
        }
    }

    private fun labelMetodeAmbil(value: String): String {
        return when (value) {
            "ambil_toko" -> "Ambil di Toko"
            "kurir_toko" -> "Kurir Toko"
            else -> value.replace("_", " ").replaceFirstChar { it.uppercase() }
        }
    }

    private fun labelMetodeBayar(value: String): String {
        return when (value) {
            "cod" -> "Cash / COD"
            "transfer_bank" -> "Transfer Bank"
            else -> value.replace("_", " ").replaceFirstChar { it.uppercase() }
        }
    }

    private fun labelStatus(status: String): String {
        return when (status) {
            "menunggu_pembayaran" -> "Menunggu Pembayaran"
            "menunggu_verifikasi" -> "Menunggu Verifikasi"
            "menunggu_konfirmasi" -> "Menunggu Konfirmasi"
            "diproses" -> "Diproses"
            "disiapkan" -> "Disiapkan"
            "siap_diambil" -> "Siap Diambil"
            "dalam_pengantaran" -> "Dalam Pengantaran"
            "selesai" -> "Selesai"
            "dibatalkan" -> "Dibatalkan"
            "dibayar" -> "Dibayar / Lunas"
            "ditolak" -> "Ditolak"
            "gagal" -> "Gagal"
            "kedaluwarsa" -> "Kedaluwarsa"
            else -> status.replace("_", " ").replaceFirstChar { it.uppercase() }
        }
    }

    private fun formatRupiah(value: Double): String {
        return NumberFormat
            .getCurrencyInstance(Locale("id", "ID"))
            .format(value)
            .replace(",00", "")
    }

    private fun getColorCompat(id: Int): Int {
        return androidx.core.content.ContextCompat.getColor(this, id)
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}