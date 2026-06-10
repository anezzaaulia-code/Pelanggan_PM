package anezza.aulia.pelanggan_pm.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import anezza.aulia.pelanggan_pm.UlasanActivity
import anezza.aulia.pelanggan_pm.adapter.PesananAdapter
import anezza.aulia.pelanggan_pm.databinding.FragmentPesananBinding
import anezza.aulia.pelanggan_pm.helper.ApiConfig
import anezza.aulia.pelanggan_pm.helper.SessionManager
import anezza.aulia.pelanggan_pm.model.ItemPesanan
import anezza.aulia.pelanggan_pm.model.Pesanan
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject

class PesananFragment : Fragment() {

    private var _b: FragmentPesananBinding? = null
    private val b get() = _b!!

    private lateinit var session: SessionManager
    private val listPesanan = ArrayList<Pesanan>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _b = FragmentPesananBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        session = SessionManager(requireContext())

        b.rvPesanan.layoutManager = LinearLayoutManager(requireContext())

        loadPesanan()
    }

    override fun onResume() {
        super.onResume()

        if (_b != null && isAdded) {
            loadPesanan()
        }
    }

    private fun loadPesanan() {
        val request = object : StringRequest(
            Request.Method.GET,
            ApiConfig.ORDERS,
            { response ->
                if (_b != null && isAdded) {
                    try {
                        listPesanan.clear()

                        val root = JSONObject(response)
                        val data = root.optJSONObject("data")
                        val arr = data?.optJSONArray("data") ?: JSONArray()

                        for (i in 0 until arr.length()) {
                            val o = arr.getJSONObject(i)
                            val items = parseItems(o.optJSONArray("item"))
                            val produkSudahDiulas = parseProdukSudahDiulas(o.optJSONArray("ulasan"))

                            val pembayaran = o.optJSONObject("pembayaran")
                            val metodePembayaran =
                                pembayaran?.optString("metode_pembayaran", "") ?: ""

                            listPesanan.add(
                                Pesanan(
                                    id = o.optInt("id", 0),
                                    invoice = o.optString("nomor_invoice", "-"),
                                    tanggal = o.optString("tanggal_pesanan", "-"),
                                    total = o.optDouble("total_bayar", 0.0),
                                    status = o.optString("status", "-"),
                                    statusPembayaran = o.optString("status_pembayaran", "-"),
                                    metodePengambilan = o.optString("metode_pengambilan", "-"),
                                    metodePembayaran = metodePembayaran,
                                    items = items,
                                    produkSudahDiulas = produkSudahDiulas
                                )
                            )
                        }

                        b.rvPesanan.adapter = PesananAdapter(
                            requireContext(),
                            listPesanan,
                            onKonfirmasiDiterima = { pesanan ->
                                konfirmasiDiterima(pesanan)
                            },
                            onBeriUlasan = { pesanan ->
                                bukaUlasan(pesanan)
                            },
                            onDetail = { pesanan ->
                                Toast.makeText(
                                    requireContext(),
                                    "Invoice: ${pesanan.invoice}\nStatus: ${pesanan.status}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        )
                    } catch (e: Exception) {
                        Toast.makeText(
                            requireContext(),
                            "Data pesanan gagal dibaca",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            },
            { error ->
                if (_b != null && isAdded) {
                    val msg = error.networkResponse?.data?.let {
                        try {
                            JSONObject(String(it)).optString("message", "Gagal memuat pesanan")
                        } catch (e: Exception) {
                            "Gagal memuat pesanan"
                        }
                    } ?: "Tidak bisa terhubung ke server"

                    Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
                }
            }
        ) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): MutableMap<String, String> {
                return hashMapOf(
                    "Authorization" to "Bearer ${session.token()}",
                    "Accept" to "application/json"
                )
            }
        }

        Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun parseItems(arr: JSONArray?): ArrayList<ItemPesanan> {
        val items = ArrayList<ItemPesanan>()

        if (arr == null) return items

        for (i in 0 until arr.length()) {
            val item = arr.getJSONObject(i)
            val produk = item.optJSONObject("produk")

            items.add(
                ItemPesanan(
                    id = item.optInt("id", 0),
                    produkId = item.optInt("produk_id", produk?.optInt("id", 0) ?: 0),
                    namaProduk = produk?.optString("nama", "Produk") ?: "Produk",
                    jumlah = item.optInt("jumlah", 0),
                    hargaSatuan = item.optDouble("harga_satuan", 0.0),
                    subtotal = item.optDouble("subtotal", 0.0)
                )
            )
        }

        return items
    }

    private fun parseProdukSudahDiulas(arr: JSONArray?): ArrayList<Int> {
        val ids = ArrayList<Int>()

        if (arr == null) return ids

        for (i in 0 until arr.length()) {
            val item = arr.getJSONObject(i)
            val produkId = item.optInt("produk_id", 0)

            if (produkId > 0 && !ids.contains(produkId)) {
                ids.add(produkId)
            }
        }

        return ids
    }

    private fun konfirmasiDiterima(pesanan: Pesanan) {
        val request = object : StringRequest(
            Request.Method.PATCH,
            "${ApiConfig.ORDERS}/${pesanan.id}/received",
            { response ->
                if (_b != null && isAdded) {
                    val obj = JSONObject(response)

                    Toast.makeText(
                        requireContext(),
                        obj.optString("message", "Pesanan berhasil dikonfirmasi"),
                        Toast.LENGTH_SHORT
                    ).show()

                    loadPesanan()
                }
            },
            { error ->
                if (_b != null && isAdded) {
                    val msg = error.networkResponse?.data?.let {
                        try {
                            JSONObject(String(it)).optString("message", "Gagal konfirmasi pesanan")
                        } catch (e: Exception) {
                            "Gagal konfirmasi pesanan"
                        }
                    } ?: "Tidak bisa terhubung ke server"

                    Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
                }
            }
        ) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): MutableMap<String, String> {
                return hashMapOf(
                    "Authorization" to "Bearer ${session.token()}",
                    "Accept" to "application/json"
                )
            }
        }

        Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun bukaUlasan(pesanan: Pesanan) {
        val itemBelumDiulas = pesanan.itemBelumDiulas()

        if (itemBelumDiulas == null) {
            Toast.makeText(requireContext(), "Semua produk pada pesanan ini sudah diulas", Toast.LENGTH_SHORT)
                .show()
            loadPesanan()
            return
        }

        val intent = Intent(requireContext(), UlasanActivity::class.java)
        intent.putExtra("pesanan_id", pesanan.id)
        intent.putExtra("produk_id", itemBelumDiulas.produkId)
        intent.putExtra("nama_produk", itemBelumDiulas.namaProduk)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}