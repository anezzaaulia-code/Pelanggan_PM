package anezza.aulia.pelanggan_pm.model

data class Pesanan(
    val id: Int,
    val invoice: String,
    val tanggal: String,
    val total: Double,
    val status: String,
    val statusPembayaran: String,
    val metodePengambilan: String,
    val metodePembayaran: String = "",
    val qrCode: String? = null,
    val items: ArrayList<ItemPesanan> = arrayListOf(),
    val produkSudahDiulas: ArrayList<Int> = arrayListOf()
) {
    fun itemBelumDiulas(): ItemPesanan? {
        return items.firstOrNull { !produkSudahDiulas.contains(it.produkId) }
    }

    fun semuaProdukSudahDiulas(): Boolean {
        return items.isNotEmpty() && items.all { produkSudahDiulas.contains(it.produkId) }
    }

    fun bisaTampilQrAmbil(): Boolean {
        return metodePengambilan == "ambil_toko"
                && status == "siap_diambil"
    }
}

data class ItemPesanan(
    val id: Int,
    val produkId: Int,
    val namaProduk: String,
    val jumlah: Int,
    val hargaSatuan: Double,
    val subtotal: Double
)