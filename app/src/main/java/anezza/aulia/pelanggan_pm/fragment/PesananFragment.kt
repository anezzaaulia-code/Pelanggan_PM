package anezza.aulia.pelanggan_pm.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.AuthFailureError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import anezza.aulia.pelanggan_pm.adapter.PesananAdapter
import anezza.aulia.pelanggan_pm.databinding.FragmentPesananBinding
import anezza.aulia.pelanggan_pm.helper.ApiConfig
import anezza.aulia.pelanggan_pm.helper.SessionManager
import anezza.aulia.pelanggan_pm.model.Pesanan

class PesananFragment : Fragment() {

    private var _b: FragmentPesananBinding? = null
    private val b get() = _b!!

    private lateinit var session: SessionManager
    private val listPesanan = ArrayList<Pesanan>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentPesananBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        session = SessionManager(requireContext())
        b.rvPesanan.layoutManager = LinearLayoutManager(requireContext())
        loadPesanan()
    }

    private fun loadPesanan() {
        val request = object : StringRequest(
            Method.GET,
            ApiConfig.ORDERS,
            { response ->
                listPesanan.clear()

                val data = JSONObject(response).getJSONObject("data")
                val arr = data.getJSONArray("data")

                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)

                    listPesanan.add(
                        Pesanan(
                            id = o.getInt("id"),
                            invoice = o.getString("nomor_invoice"),
                            tanggal = o.getString("tanggal_pesanan"),
                            total = o.getDouble("total_bayar"),
                            status = o.getString("status"),
                            statusPembayaran = o.getString("status_pembayaran"),
                            metodePengambilan = o.getString("metode_pengambilan")
                        )
                    )
                }

                b.rvPesanan.adapter = PesananAdapter(requireContext(), listPesanan)
            },
            {}
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

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}