package anezza.aulia.pelanggan_pm.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import anezza.aulia.pelanggan_pm.databinding.ItemPesananBinding
import anezza.aulia.pelanggan_pm.model.Pesanan
import java.text.NumberFormat
import java.util.Locale

class PesananAdapter(
    private val context: Context,
    private val list: ArrayList<Pesanan>,
    private val onKonfirmasiDiterima: (Pesanan) -> Unit,
    private val onBeriUlasan: (Pesanan) -> Unit,
    private val onDetail: (Pesanan) -> Unit,
    private val onTampilQr: (Pesanan) -> Unit
) : RecyclerView.Adapter<PesananAdapter.ViewHolder>() {

    inner class ViewHolder(val b: ItemPesananBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val b = ItemPesananBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(b)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val p = list[position]

        holder.b.txtInvoice.text = p.invoice
        holder.b.txtStatus.text = labelStatus(p.status)

        val metodeTerima = when (p.metodePengambilan) {
            "ambil_toko" -> "Ambil di Toko"
            "kurir_toko" -> "Kurir Toko"
            else -> p.metodePengambilan
        }

        val metodeBayar = when (p.metodePembayaran) {
            "cod" -> "Cash / COD"
            "transfer_bank" -> "Transfer Bank"
            else -> p.metodePembayaran.ifEmpty { "-" }
        }

        val produkText = if (p.items.isNotEmpty()) {
            p.items.joinToString(", ") { "${it.namaProduk} x${it.jumlah}" }
        } else {
            "Detail produk belum tersedia"
        }

        holder.b.txtDetailPesanan.text =
            "${p.tanggal}\n$produkText\n$metodeTerima • $metodeBayar"

        holder.b.txtTotalPesanan.text =
            "Total ${formatRupiah(p.total)} • Pembayaran: ${labelStatus(p.statusPembayaran)}"

        /*
         * QR muncul khusus pesanan ambil toko yang sudah siap diambil.
         * QR ini bukan QRIS, tapi QR validasi pengambilan pesanan.
         */
        if (p.bisaTampilQrAmbil()) {
            holder.b.btnQrPesanan.visibility = View.VISIBLE
            holder.b.btnQrPesanan.setOnClickListener {
                onTampilQr(p)
            }
        } else {
            holder.b.btnQrPesanan.visibility = View.GONE
            holder.b.btnQrPesanan.setOnClickListener(null)
        }

        /*
         * Tombol aksi utama.
         * Kalau ambil_toko + siap_diambil, jangan langsung konfirmasi diterima.
         * Pembeli harus buka QR dulu.
         */
        when {
            p.status == "siap_diambil" && p.metodePengambilan == "ambil_toko" -> {
                holder.b.btnAksiPesanan.visibility = View.VISIBLE
                holder.b.btnAksiPesanan.isEnabled = true
                holder.b.btnAksiPesanan.text = "Lihat Detail"
                holder.b.btnAksiPesanan.setOnClickListener {
                    onDetail(p)
                }
            }

            p.status == "dalam_pengantaran" -> {
                holder.b.btnAksiPesanan.visibility = View.VISIBLE
                holder.b.btnAksiPesanan.isEnabled = true
                holder.b.btnAksiPesanan.text = "Konfirmasi Diterima"
                holder.b.btnAksiPesanan.setOnClickListener {
                    onKonfirmasiDiterima(p)
                }
            }

            p.status == "selesai" && !p.semuaProdukSudahDiulas() -> {
                holder.b.btnAksiPesanan.visibility = View.VISIBLE
                holder.b.btnAksiPesanan.isEnabled = true
                holder.b.btnAksiPesanan.text = "Beri Ulasan"
                holder.b.btnAksiPesanan.setOnClickListener {
                    onBeriUlasan(p)
                }
            }

            p.status == "selesai" && p.semuaProdukSudahDiulas() -> {
                holder.b.btnAksiPesanan.visibility = View.VISIBLE
                holder.b.btnAksiPesanan.isEnabled = false
                holder.b.btnAksiPesanan.text = "Ulasan Terkirim"
                holder.b.btnAksiPesanan.setOnClickListener(null)
            }

            else -> {
                holder.b.btnAksiPesanan.visibility = View.VISIBLE
                holder.b.btnAksiPesanan.isEnabled = true
                holder.b.btnAksiPesanan.text = "Detail Pesanan"
                holder.b.btnAksiPesanan.setOnClickListener {
                    onDetail(p)
                }
            }
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
}