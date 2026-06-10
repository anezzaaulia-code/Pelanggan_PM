package anezza.aulia.pelanggan_pm.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import anezza.aulia.pelanggan_pm.R
import anezza.aulia.pelanggan_pm.adapter.ProdukAdapter
import anezza.aulia.pelanggan_pm.databinding.FragmentProdukBinding
import anezza.aulia.pelanggan_pm.helper.ApiConfig
import anezza.aulia.pelanggan_pm.model.Produk
import com.android.volley.Request
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

    private val handler = Handler(Looper.getMainLooper())

    private val sortLabel = arrayOf(
        "Terbaru",
        "Harga Terendah",
        "Harga Tertinggi",
        "Terlaris"
    )

    private val sortValue = arrayOf(
        "terbaru",
        "harga_terendah",
        "harga_tertinggi",
        "terlaris"
    )

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

        loadProduk(forceRefresh = true)
    }

    override fun onResume() {
        super.onResume()

        if (_b != null && isAdded && listProduk.isEmpty()) {
            loadProduk(forceRefresh = true)
        }
    }

    private fun setupSpinnerSort() {
        val sortAdapter = ArrayAdapter(
            requireContext(),
            R.layout.item_spinner_sort,
            sortLabel
        )

        sortAdapter.setDropDownViewResource(R.layout.item_spinner_sort)

        b.spinnerSort.adapter = sortAdapter

        b.spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val value = sortValue.getOrElse(position) { "terbaru" }

                if (!sudahSetupSpinner) {
                    sudahSetupSpinner = true
                    sortTerakhir = value
                    return
                }

                if (value != sortTerakhir) {
                    sortTerakhir = value
                    loadProduk(forceRefresh = true)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupSearch() {
        b.actCari.doAfterTextChanged {
            handler.removeCallbacksAndMessages(null)

            handler.postDelayed({
                if (_b == null || !isAdded) return@postDelayed

                val keywordSekarang = b.actCari.text.toString().trim()

                if (keywordSekarang != keywordTerakhir) {
                    keywordTerakhir = keywordSekarang
                    loadProduk(forceRefresh = true)
                }
            }, 450)
        }
    }

    private fun loadProduk(forceRefresh: Boolean = false) {
        if (_b == null || !isAdded) return
        if (sedangLoad) return
        if (!forceRefresh && listProduk.isNotEmpty()) return

        sedangLoad = true

        val keyword = b.actCari.text.toString().trim()
        val q = URLEncoder.encode(keyword, "UTF-8")

        val sortPosition = b.spinnerSort.selectedItemPosition.takeIf { it >= 0 } ?: 0
        val sort = sortValue.getOrElse(sortPosition) { sortTerakhir }

        val url = "${ApiConfig.PRODUCTS}?q=$q&sort=$sort&per_page=50"

        val request = StringRequest(
            Request.Method.GET,
            url,
            { response ->
                sedangLoad = false

                if (_b != null && isAdded) {
                    try {
                        val produkBaru = ArrayList<Produk>()

                        val obj = JSONObject(response)
                        val data = obj.optJSONObject("data")
                        val arr = data?.optJSONArray("data")

                        if (arr != null) {
                            for (i in 0 until arr.length()) {
                                val o = arr.getJSONObject(i)

                                val masaSimpanValue: Int? =
                                    if (o.isNull("masa_simpan")) null else o.optInt("masa_simpan", 0)

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
                                        deskripsi = o.optString("deskripsi", ""),
                                        masaSimpan = masaSimpanValue,
                                        saranPenyimpanan = o.optString("saran_penyimpanan", ""),
                                        saranPenyajian = o.optString("saran_penyajian", "")
                                    )
                                )
                            }
                        }

                        listProduk.clear()
                        listProduk.addAll(produkBaru)
                        adapterProduk?.notifyDataSetChanged()

                        b.txtJumlahProduk.text =
                            if (produkBaru.isEmpty()) "Kosong" else "${produkBaru.size} Produk"

                        b.txtJudulProduk.text =
                            if (keyword.isEmpty()) "Semua Produk" else "Hasil Pencarian"
                    } catch (e: Exception) {
                        b.txtJumlahProduk.text = "Gagal"
                    }
                }
            },
            {
                sedangLoad = false

                if (_b != null && isAdded) {
                    b.txtJumlahProduk.text = "Offline"
                }
            }
        )

        Volley.newRequestQueue(requireContext()).add(request)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
        _b = null
    }
}