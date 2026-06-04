package anezza.aulia.pelanggan_pm.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import anezza.aulia.pelanggan_pm.databinding.ItemPesananBinding
import anezza.aulia.pelanggan_pm.model.Pesanan
import java.text.NumberFormat
import java.util.Locale

class PesananAdapter(
    private val context: Context,
    private val list: ArrayList<Pesanan>
) : RecyclerView.Adapter<PesananAdapter.ViewHolder>() {

    inner class ViewHolder(val b: ItemPesananBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val b = ItemPesananBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(b)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val p = list[position]

        holder.b.txtInvoice.text = p.invoice
        holder.b.txtStatus.text = p.status

        holder.b.txtDetailPesanan.text = p.tanggal
        holder.b.txtTotalPesanan.text =
            "Total ${formatRupiah(p.total)} • Pembayaran: ${p.statusPembayaran}"

        holder.b.btnAksiPesanan.text = when (p.status.lowercase()) {
            "selesai" -> "Beri Ulasan"
            "menunggu" -> "Lihat QR Pembayaran"
            "pending" -> "Lihat QR Pembayaran"
            "dalam pengantaran" -> "Lihat Status Pesanan"
            else -> "Detail Pesanan"
        }
    }

    private fun formatRupiah(value: Double): String {
        return NumberFormat
            .getCurrencyInstance(Locale("id", "ID"))
            .format(value)
            .replace(",00", "")
    }
}