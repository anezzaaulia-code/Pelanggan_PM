package anezza.aulia.pelanggan_pm.model

data class UlasanProduk(
    val id: Int,
    val user: String,
    val rating: Int,
    val komentar: String,
    val fotoUlasan: String?,
    val videoUlasan: String?,
    val tanggal: String
)