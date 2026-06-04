package anezza.aulia.pelanggan_pm.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import anezza.aulia.pelanggan_pm.adapter.ProdukAdapter
import anezza.aulia.pelanggan_pm.databinding.FragmentProdukBinding
import anezza.aulia.pelanggan_pm.helper.ApiConfig
import anezza.aulia.pelanggan_pm.model.Produk

class ProdukFragment : Fragment() {

    private var _b: FragmentProdukBinding? = null
    private val b get() = _b!!

    private val listProduk = ArrayList<Produk>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentProdukBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        b.rvProduk.layoutManager = LinearLayoutManager(requireContext())

        val sortList = arrayOf("terbaru", "harga_terendah", "harga_tertinggi", "terlaris")
        b.spinnerSort.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, sortList)

        loadProduk()

        b.actCari.doAfterTextChanged {
            loadProduk()
        }

        b.spinnerSort.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                loadProduk()
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })
    }

    private fun loadProduk() {
        val q = b.actCari.text.toString()
        val sort = b.spinnerSort.selectedItem?.toString() ?: "terbaru"

        val url = ApiConfig.PRODUCTS + "?q=$q&sort=$sort&per_page=50"

        val request = StringRequest(
            url,
            { response ->
                listProduk.clear()
                val data = JSONObject(response).getJSONObject("data")
                val arr = data.getJSONArray("data")

                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)

                    listProduk.add(
                        Produk(
                            id = o.getInt("id"),
                            nama = o.getString("nama"),
                            harga = o.getDouble("harga"),
                            stok = o.getInt("stok"),
                            satuan = o.getString("satuan"),
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

                b.rvProduk.adapter = ProdukAdapter(requireContext(), listProduk)
            },
            {}
        )

        Volley.newRequestQueue(requireContext()).add(request)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}