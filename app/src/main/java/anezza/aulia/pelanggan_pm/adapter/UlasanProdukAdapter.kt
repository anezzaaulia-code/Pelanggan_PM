package anezza.aulia.pelanggan_pm.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import anezza.aulia.pelanggan_pm.R
import anezza.aulia.pelanggan_pm.databinding.ItemUlasanProdukBinding
import anezza.aulia.pelanggan_pm.helper.ApiConfig
import anezza.aulia.pelanggan_pm.model.UlasanProduk
import com.squareup.picasso.Picasso

class UlasanProdukAdapter(
    private val context: Context,
    private val list: ArrayList<UlasanProduk>
) : RecyclerView.Adapter<UlasanProdukAdapter.ViewHolder>() {

    inner class ViewHolder(val b: ItemUlasanProdukBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val b = ItemUlasanProdukBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(b)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        val namaUser = item.user.ifEmpty { "Pembeli" }
        val avatar = namaUser.firstOrNull()?.uppercaseChar()?.toString() ?: "P"

        holder.b.txtAvatar.text = avatar
        holder.b.txtNamaUser.text = namaUser
        holder.b.txtRating.text = "⭐ ${item.rating}"
        holder.b.txtTanggal.text = item.tanggal.ifEmpty { "Tanggal ulasan tidak tersedia" }
        holder.b.txtKomentar.text = item.komentar.ifEmpty { "Tidak ada komentar." }

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

        holder.b.txtVideoUlasan.visibility =
            if (!item.videoUlasan.isNullOrEmpty()) View.VISIBLE else View.GONE
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