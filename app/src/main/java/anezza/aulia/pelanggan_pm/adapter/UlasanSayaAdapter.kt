package anezza.aulia.pelanggan_pm.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import anezza.aulia.pelanggan_pm.R
import anezza.aulia.pelanggan_pm.databinding.ItemUlasanSayaBinding
import anezza.aulia.pelanggan_pm.helper.ApiConfig
import anezza.aulia.pelanggan_pm.model.UlasanSaya
import com.squareup.picasso.Picasso

class UlasanSayaAdapter(
    private val context: Context,
    private val list: ArrayList<UlasanSaya>
) : RecyclerView.Adapter<UlasanSayaAdapter.ViewHolder>() {

    inner class ViewHolder(val b: ItemUlasanSayaBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val b = ItemUlasanSayaBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(b)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.b.txtNamaProduk.text = item.namaProduk
        holder.b.txtInvoice.text = item.nomorInvoice.ifEmpty { "Invoice tidak tersedia" }
        holder.b.txtRating.text = "⭐ ${item.rating}"
        holder.b.txtKomentar.text = item.komentar.ifEmpty { "Tidak ada komentar." }
        holder.b.txtTanggal.text = item.tanggal.ifEmpty { "Tanggal ulasan tidak tersedia" }

        val fotoUrl = normalisasiUrlMedia(item.fotoUlasan)

        if (!fotoUrl.isNullOrEmpty()) {
            holder.b.imgFotoUlasan.visibility = View.VISIBLE

            Picasso.get()
                .load(fotoUrl)
                .placeholder(R.color.hijau_muda)
                .error(R.color.hijau_muda)
                .into(holder.b.imgFotoUlasan)
        } else {
            holder.b.imgFotoUlasan.visibility = View.GONE
        }

        holder.b.txtVideo.visibility =
            if (!item.videoUlasan.isNullOrEmpty() && item.videoUlasan != "null") {
                View.VISIBLE
            } else {
                View.GONE
            }
    }

    private fun normalisasiUrlMedia(path: String?): String? {
        if (path.isNullOrEmpty() || path == "null") return null

        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path
        }

        val baseHost = ApiConfig.BASE_URL
            .removeSuffix("/api")
            .removeSuffix("/")

        val cleanPath = path.removePrefix("/")

        return when {
            cleanPath.startsWith("storage/") -> "$baseHost/$cleanPath"
            else -> "$baseHost/storage/$cleanPath"
        }
    }
}