package anezza.aulia.pelanggan_pm.model

data class Produk(
    val id: Int,
    val nama: String,
    val harga: Double,
    val stok: Int,
    val satuan: String,
    val isiPerSatuan: Int,
    val berat: Double,
    val gambarUtama: String?,
    val deskripsi: String?,
    val masaSimpan: Int?,
    val saranPenyimpanan: String?,
    val saranPenyajian: String?
)