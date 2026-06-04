package anezza.aulia.pelanggan_pm.model

data class CartItem(
    val id: Int = 0,
    val produkId: Int,
    val nama: String,
    val harga: Double,
    val stok: Int,
    val satuan: String,
    val gambar: String?,
    var jumlah: Int
)