package anezza.aulia.pelanggan_pm.model

data class Pesanan(
    val id: Int,
    val invoice: String,
    val tanggal: String,
    val total: Double,
    val status: String,
    val statusPembayaran: String,
    val metodePengambilan: String
)