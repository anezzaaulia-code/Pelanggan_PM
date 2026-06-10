package anezza.aulia.pelanggan_pm.model

data class UlasanSaya(
    val id: Int,
    val pesananId: Int,
    val nomorInvoice: String,
    val produkId: Int,
    val namaProduk: String,
    val rating: Int,
    val komentar: String,
    val fotoUlasan: String?,
    val videoUlasan: String?,
    val tanggal: String
)