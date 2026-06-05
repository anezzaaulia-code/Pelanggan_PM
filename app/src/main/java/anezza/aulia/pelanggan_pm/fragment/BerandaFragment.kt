package anezza.aulia.pelanggan_pm.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import anezza.aulia.pelanggan_pm.R
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
    private var adapterProduk: ProdukAdapter? = null

    private var nomorWa = ""
    private var sedangLoadStore = false
    private var sedangLoadProduk = false
    private var storeSudahLoad = false

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

        b.rvProdukBeranda.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        adapterProduk = ProdukAdapter(requireContext(), listProduk)
        b.rvProdukBeranda.adapter = adapterProduk

        if (!storeSudahLoad) {
            loadStore()
        }

        if (listProduk.isEmpty()) {
            loadProduk()
        }

        b.btnLihatProduk.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frameContainer, ProdukFragment())
                .commit()
        }

        b.btnInfoToko.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frameContainer, ProfilFragment())
                .commit()
        }

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

    override fun onResume() {
        super.onResume()

        // Jangan reload tiap balik tab.
        // Cukup load kalau data belum ada.
        if (_b != null && isAdded && listProduk.isEmpty()) {
            loadProduk()
        }

        if (_b != null && isAdded && !storeSudahLoad) {
            loadStore()
        }
    }

    private fun loadStore() {
        if (_b == null || !isAdded) return
        if (sedangLoadStore) return

        sedangLoadStore = true

        val request = StringRequest(
            ApiConfig.STORE,
            { response ->
                sedangLoadStore = false

                if (_b == null || !isAdded) return@StringRequest

                try {
                    val data = JSONObject(response).optJSONObject("data")

                    b.txtNamaToko.text = data?.optString("nama", "TahuKu") ?: "TahuKu"
                    b.txtAlamatToko.text = data?.optString("alamat", "Alamat toko belum diisi")
                        ?: "Alamat toko belum diisi"
                    b.txtJamToko.text = data?.optString("jam_buka", "07:00") ?: "07:00"

                    nomorWa = data?.optString("telepon", "") ?: ""
                    storeSudahLoad = true

                } catch (e: Exception) {
                    // Pakai default, jangan crash.
                    b.txtNamaToko.text = "TahuKu"
                    b.txtAlamatToko.text = "Alamat toko belum diisi"
                    b.txtJamToko.text = "07:00"
                    nomorWa = ""
                }
            },
            {
                sedangLoadStore = false

                if (_b == null || !isAdded) return@StringRequest

                // Default tetap tampil.
                b.txtNamaToko.text = "TahuKu"
                b.txtAlamatToko.text = "Alamat toko belum diisi"
                b.txtJamToko.text = "07:00"
                nomorWa = ""
            }
        )

        Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun loadProduk() {
        if (_b == null || !isAdded) return
        if (sedangLoadProduk) return

        sedangLoadProduk = true

        val request = StringRequest(
            ApiConfig.PRODUCTS + "?per_page=5",
            { response ->
                sedangLoadProduk = false

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

                    // Ganti data setelah request sukses.
                    listProduk.clear()
                    listProduk.addAll(produkBaru)
                    adapterProduk?.notifyDataSetChanged()

                } catch (e: Exception) {
                    // Jangan clear data lama.
                }
            },
            {
                sedangLoadProduk = false

                if (_b == null || !isAdded) return@StringRequest

                // Jangan clear data lama saat gagal.
            }
        )

        Volley.newRequestQueue(requireContext()).add(request)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}