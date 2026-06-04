package anezza.aulia.pelanggan_pm.model

data class Alamat(
    val id: Int,
    val namaPenerima: String,
    val telepon: String,
    val alamatLengkap: String,
    val latitude: Double?,
    val longitude: Double?,
    val utama: Boolean
)