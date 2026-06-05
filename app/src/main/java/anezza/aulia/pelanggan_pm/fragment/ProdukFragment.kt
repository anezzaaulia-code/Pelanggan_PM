package anezza.aulia.pelanggan_pm.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import anezza.aulia.pelanggan_pm.adapter.ProdukAdapter
import anezza.aulia.pelanggan_pm.databinding.FragmentProdukBinding
import anezza.aulia.pelanggan_pm.helper.ApiConfig
import anezza.aulia.pelanggan_pm.model.Produk
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.net.URLEncoder

class ProdukFragment : Fragment() {

    private var _b: FragmentProdukBinding? = null
    private val b get() = _b!!

    private val listProduk = ArrayList<Produk>()
    private var adapterProduk: ProdukAdapter? = null

    private var sedangLoad = false
    private var sudahSetupSpinner = false
    private var keywordTerakhir = ""
    private var sortTerakhir = "terbaru"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _b = FragmentProdukBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        b.rvProduk.layoutManager = GridLayoutManager(requireContext(), 2)

        adapterProduk = ProdukAdapter(requireContext(), listProduk)
        b.rvProduk.adapter = adapterProduk

        setupSpinnerSort()
        setupSearch()

        loadProduk(forceRefresh = listProduk.isEmpty())
    }

    override fun onResume() {
        super.onResume()

        // Jangan reload setiap balik dari Keranjang.
        // Reload cuma kalau list masih kosong.
        if (_b != null && isAdded && listProduk.isEmpty()) {
            loadProduk(forceRefresh = true)
        }
    }

    private fun setupSpinnerSort() {
        val sortList = arrayOf("terbaru", "harga_terendah", "harga_tertinggi", "terlaris")

        val sortAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            sortList
        )
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        b.spinnerSort.adapter = sortAdapter

        b.spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val sortSekarang = b.spinnerSort.selectedItem?.toString() ?: "terbaru"

                if (!sudahSetupSpinner) {
                    sudahSetupSpinner = true
                    sortTerakhir = sortSekarang
                    return
                }

                if (sortSekarang != sortTerakhir) {
                    sortTerakhir = sortSekarang
                    loadProduk(forceRefresh = true)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupSearch() {
        b.actCari.doAfterTextChanged {
            if (_b == null || !isAdded) return@doAfterTextChanged

            val keywordSekarang = b.actCari.text.toString().trim()

            if (keywordSekarang != keywordTerakhir) {
                keywordTerakhir = keywordSekarang
                loadProduk(forceRefresh = true)
            }
        }
    }

    private fun loadProduk(forceRefresh: Boolean = false) {
        if (_b == null || !isAdded) return
        if (sedangLoad) return

        // Kalau data sudah ada dan tidak dipaksa refresh, jangan load ulang.
        if (!forceRefresh && listProduk.isNotEmpty()) return

        sedangLoad = true

        val q = URLEncoder.encode(b.actCari.text.toString().trim(), "UTF-8")
        val sort = b.spinnerSort.selectedItem?.toString() ?: "terbaru"

        val url = "${ApiConfig.PRODUCTS}?q=$q&sort=$sort&per_page=50"

        val request = StringRequest(
            url,
            { response ->
                sedangLoad = false

                if (_b == null || !isAdded) return@StringRequest

                try {
                    val produkBaru = ArrayList<Produk>()

                    val obj = JSONObject(response)
                    val data = obj.optJSONObject("data")
                    val arr = data?.optJSONArray("data")

                    if (arr != null) {
                        for (i in 0 until arr.length()) {
                            val o = arr.getJSONObject(i)

                            produkBaru.add(
                                Produk(
                                    id = o.optInt("id", 0),
                                    nama = o.optString("nama", "-"),
                                    harga = o.optDouble("harga", 0.0),
                                    stok = o.optInt("stok", 0),
                                    satuan = o.optString("satuan", ""),
                                    isiPerSatuan = o.optInt("isi_per_satuan", 0),
                                    berat = o.optDouble("berat", 0.0),
                                    gambarUtama = o.optString("gambar_utama", ""),
                                    deskripsi = null,
                                    masaSimpan = null,
                                    saranPenyimpanan = null,
                                    saranPenyajian = null
                                )
                            )
                        }
                    }

                    // Baru ganti data setelah parsing sukses.
                    // Jadi nggak ada efek produk tiba-tiba kosong pas reload.
                    listProduk.clear()
                    listProduk.addAll(produkBaru)
                    adapterProduk?.notifyDataSetChanged()

                } catch (e: Exception) {
                    // Jangan clear list saat parsing error.
                    // Produk lama tetap tampil.
                }
            },
            {
                sedangLoad = false

                if (_b == null || !isAdded) return@StringRequest

                // Jangan clear list saat request gagal.
                // Produk lama tetap tampil.
            }
        )

        Volley.newRequestQueue(requireContext()).add(request)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}