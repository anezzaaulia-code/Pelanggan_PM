package anezza.aulia.pelanggan_pm.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import anezza.aulia.pelanggan_pm.DetailProdukActivity
import anezza.aulia.pelanggan_pm.R
import anezza.aulia.pelanggan_pm.databinding.ItemProdukBinding
import anezza.aulia.pelanggan_pm.model.Produk
import com.squareup.picasso.Picasso
import java.text.NumberFormat
import java.util.Locale

class ProdukAdapter(
    private val context: Context,
    private val list: ArrayList<Produk>
) : RecyclerView.Adapter<ProdukAdapter.ViewHolder>() {

    inner class ViewHolder(val b: ItemProdukBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val b = ItemProdukBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(b)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val p = list[position]

        holder.b.txtNama.text = p.nama
        holder.b.txtHarga.text = formatRupiah(p.harga)
        holder.b.txtStok.text = "Stok ${p.stok} ${p.satuan}"

        holder.b.txtBadge.text = when {
            p.stok <= 5 -> "Tipis"
            position == 0 -> "Baru"
            position == 1 -> "Laris"
            else -> "Fresh"
        }

        if (!p.gambarUtama.isNullOrEmpty()) {
            holder.b.imgProduk.visibility = View.VISIBLE
            holder.b.txtIconProduk.visibility = View.GONE

            Picasso.get()
                .load(p.gambarUtama)
                .placeholder(R.color.hijau_muda)
                .error(R.color.hijau_muda)
                .into(holder.b.imgProduk)
        } else {
            holder.b.imgProduk.visibility = View.GONE
            holder.b.txtIconProduk.visibility = View.VISIBLE
        }

        holder.b.btnDetail.setOnClickListener {
            bukaDetail(p.id)
        }

        holder.itemView.setOnClickListener {
            bukaDetail(p.id)
        }
    }

    private fun bukaDetail(id: Int) {
        val intent = Intent(context, DetailProdukActivity::class.java)
        intent.putExtra("id", id)
        context.startActivity(intent)
    }

    private fun formatRupiah(value: Double): String {
        return NumberFormat
            .getCurrencyInstance(Locale("id", "ID"))
            .format(value)
            .replace(",00", "")
    }
}