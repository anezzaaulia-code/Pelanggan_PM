package anezza.aulia.pelanggan_pm.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import anezza.aulia.pelanggan_pm.adapter.ProdukAdapter
import anezza.aulia.pelanggan_pm.databinding.FragmentBerandaBinding
import anezza.aulia.pelanggan_pm.helper.ApiConfig
import anezza.aulia.pelanggan_pm.model.Produk
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class BerandaFragment : Fragment() {

    private var _b: FragmentBerandaBinding? = null
    private val b get() = _b!!

    private val listProduk = ArrayList<Produk>()
    private var nomorWa = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _b = FragmentBerandaBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        b.rvProdukBeranda.layoutManager = LinearLayoutManager(requireContext())

        loadStore()
        loadProduk()

        b.btnWhatsapp.setOnClickListener {
            if (nomorWa.isNotEmpty()) {
                val nomorBersih = nomorWa
                    .replace("+", "")
                    .replace(" ", "")
                    .replace("-", "")

                val url = "https://wa.me/$nomorBersih"
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        }
    }

    private fun loadStore() {
        val request = StringRequest(
            ApiConfig.STORE,
            { response ->
                val data = JSONObject(response).getJSONObject("data")

                b.txtNamaToko.text = data.optString("nama", "TahuKu")
                b.txtAlamatToko.text = data.optString("alamat", "Alamat toko belum diisi")
                b.txtJamToko.text = data.optString("jam_buka", "07:00")

                nomorWa = data.optString("telepon", "")
            },
            {
                b.txtNamaToko.text = "TahuKu"
                b.txtAlamatToko.text = "Alamat toko belum diisi"
                b.txtJamToko.text = "07:00"
            }
        )

        Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun loadProduk() {
        val request = StringRequest(
            ApiConfig.PRODUCTS + "?per_page=5",
            { response ->
                listProduk.clear()

                val obj = JSONObject(response)
                val data = obj.getJSONObject("data")
                val arr = data.getJSONArray("data")

                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)

                    listProduk.add(
                        Produk(
                            id = o.getInt("id"),
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

                b.rvProdukBeranda.adapter = ProdukAdapter(requireContext(), listProduk)
            },
            {
                // Kalau gagal, biarkan kosong dulu.
            }
        )

        Volley.newRequestQueue(requireContext()).add(request)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}