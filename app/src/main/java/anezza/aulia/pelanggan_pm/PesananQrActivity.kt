package anezza.aulia.pelanggan_pm

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import anezza.aulia.pelanggan_pm.databinding.ActivityPesananQrBinding
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

class PesananQrActivity : AppCompatActivity() {

    private lateinit var b: ActivityPesananQrBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityPesananQrBinding.inflate(layoutInflater)
        setContentView(b.root)

        val invoice = intent.getStringExtra("invoice") ?: "-"
        val qrCode = intent.getStringExtra("qr_code") ?: ""
        val statusPesanan = intent.getStringExtra("status_pesanan") ?: "-"
        val statusPembayaran = intent.getStringExtra("status_pembayaran") ?: "-"
        val total = intent.getStringExtra("total") ?: "-"

        b.txtInvoiceQr.text = invoice

        b.txtStatusQr.text =
            "Status Pesanan: ${labelStatus(statusPesanan)}\n" +
                    "Status Pembayaran: ${labelStatus(statusPembayaran)}\n" +
                    "Total: $total"

        if (qrCode.isBlank()) {
            Toast.makeText(this, "QR Code pesanan belum tersedia", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        b.imgQrPesanan.setImageBitmap(generateQrBitmap(qrCode, 700, 700))
    }

    private fun generateQrBitmap(text: String, width: Int, height: Int): Bitmap {
        val matrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, width, height)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (matrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }

        return bitmap
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
}