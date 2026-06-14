package anezza.aulia.pelanggan_pm.adapter

import android.content.Context
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import anezza.aulia.pelanggan_pm.databinding.ItemCartBinding
import anezza.aulia.pelanggan_pm.helper.CartDbHelper
import anezza.aulia.pelanggan_pm.model.CartItem
import java.text.NumberFormat
import java.util.Locale

class CartAdapter(
    private val context: Context,
    private val list: ArrayList<CartItem>,
    private val onChanged: () -> Unit
) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    private val db = CartDbHelper(context)

    inner class ViewHolder(val b: ItemCartBinding) : RecyclerView.ViewHolder(b.root),
        View.OnCreateContextMenuListener {

        override fun onCreateContextMenu(
            menu: ContextMenu?,
            view: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            val currentPosition = adapterPosition

            if (currentPosition == RecyclerView.NO_POSITION || menu == null) {
                return
            }

            val item = list[currentPosition]

            menu.setHeaderTitle(item.nama)

            menu.add("Tambah jumlah").setOnMenuItemClickListener {
                if (item.jumlah < item.stok) {
                    item.jumlah++
                    db.updateJumlah(item.id, item.jumlah)
                    notifyItemChanged(currentPosition)
                    onChanged()
                }

                true
            }

            menu.add("Kurangi jumlah").setOnMenuItemClickListener {
                if (item.jumlah > 1) {
                    item.jumlah--
                    db.updateJumlah(item.id, item.jumlah)
                    notifyItemChanged(currentPosition)
                    onChanged()
                } else {
                    hapusItem(currentPosition)
                }

                true
            }

            menu.add("Hapus dari keranjang").setOnMenuItemClickListener {
                hapusItem(currentPosition)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val b = ItemCartBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(b)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.b.txtNamaCart.text = item.nama
        holder.b.txtHargaCart.text = "${formatRupiah(item.harga)} / ${item.satuan}"
        holder.b.txtJumlah.text = item.jumlah.toString()
        holder.b.txtSubtotalCart.text = formatRupiah(item.harga * item.jumlah)

        holder.b.root.setOnCreateContextMenuListener(holder)

        holder.b.btnPlus.setOnClickListener {
            val currentPosition = holder.adapterPosition

            if (currentPosition == RecyclerView.NO_POSITION) {
                return@setOnClickListener
            }

            val currentItem = list[currentPosition]

            if (currentItem.jumlah < currentItem.stok) {
                currentItem.jumlah++
                db.updateJumlah(currentItem.id, currentItem.jumlah)

                notifyItemChanged(currentPosition)
                onChanged()
            }
        }

        holder.b.btnMinus.setOnClickListener {
            val currentPosition = holder.adapterPosition

            if (currentPosition == RecyclerView.NO_POSITION) {
                return@setOnClickListener
            }

            val currentItem = list[currentPosition]

            if (currentItem.jumlah > 1) {
                currentItem.jumlah--
                db.updateJumlah(currentItem.id, currentItem.jumlah)

                notifyItemChanged(currentPosition)
                onChanged()
            } else {
                showDeletePopup(holder, currentPosition)
            }
        }

        holder.b.root.setOnLongClickListener {
            holder.b.root.showContextMenu()
            true
        }

        holder.b.txtSubtotalCart.setOnClickListener {
            val currentPosition = holder.adapterPosition

            if (currentPosition != RecyclerView.NO_POSITION) {
                showDeletePopup(holder, currentPosition)
            }
        }
    }

    private fun showDeletePopup(holder: ViewHolder, position: Int) {
        val popup = PopupMenu(context, holder.b.root)
        popup.menu.add("Hapus dari keranjang")

        popup.setOnMenuItemClickListener {
            hapusItem(position)
            true
        }

        popup.show()
    }

    private fun hapusItem(position: Int) {
        if (position < 0 || position >= list.size) {
            return
        }

        val item = list[position]

        db.deleteItem(item.id)
        list.removeAt(position)

        notifyItemRemoved(position)
        notifyItemRangeChanged(position, list.size)
        onChanged()
    }

    private fun formatRupiah(value: Double): String {
        return NumberFormat
            .getCurrencyInstance(Locale("id", "ID"))
            .format(value)
            .replace(",00", "")
    }
}