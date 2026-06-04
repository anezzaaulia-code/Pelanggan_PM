package anezza.aulia.pelanggan_pm.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import anezza.aulia.pelanggan_pm.databinding.ItemAlamatBinding
import anezza.aulia.pelanggan_pm.model.Alamat

class AlamatAdapter(
    private val context: Context,
    private val list: ArrayList<Alamat>,
    private val onSetUtama: (Alamat) -> Unit,
    private val onHapus: (Alamat) -> Unit
) : RecyclerView.Adapter<AlamatAdapter.ViewHolder>() {

    inner class ViewHolder(val b: ItemAlamatBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val b = ItemAlamatBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(b)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val alamat = list[position]

        holder.b.txtNamaPenerima.text = alamat.namaPenerima
        holder.b.txtTelepon.text = alamat.telepon
        holder.b.txtAlamatLengkap.text = alamat.alamatLengkap

        val lat = alamat.latitude ?: 0.0
        val lng = alamat.longitude ?: 0.0
        holder.b.txtKoordinat.text = "Lat: $lat, Lng: $lng"

        holder.b.txtUtama.visibility = if (alamat.utama) View.VISIBLE else View.GONE

        holder.b.root.setOnClickListener {
            val popup = PopupMenu(context, holder.b.root)

            if (!alamat.utama) {
                popup.menu.add("Jadikan alamat utama")
            }

            popup.menu.add("Hapus alamat")

            popup.setOnMenuItemClickListener {
                when (it.title.toString()) {
                    "Jadikan alamat utama" -> onSetUtama(alamat)
                    "Hapus alamat" -> onHapus(alamat)
                }
                true
            }

            popup.show()
        }
    }
}