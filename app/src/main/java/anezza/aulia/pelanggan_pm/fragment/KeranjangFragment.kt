package anezza.aulia.pelanggan_pm.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import anezza.aulia.pelanggan_pm.CheckoutActivity
import anezza.aulia.pelanggan_pm.adapter.CartAdapter
import anezza.aulia.pelanggan_pm.databinding.FragmentKeranjangBinding
import anezza.aulia.pelanggan_pm.helper.CartDbHelper
import java.text.NumberFormat
import java.util.Locale

class KeranjangFragment : Fragment() {

    private var _b: FragmentKeranjangBinding? = null
    private val b get() = _b!!

    private lateinit var db: CartDbHelper

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentKeranjangBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        db = CartDbHelper(requireContext())
        b.rvKeranjang.layoutManager = LinearLayoutManager(requireContext())

        loadCart()

        b.btnCheckout.setOnClickListener {
            startActivity(Intent(requireContext(), CheckoutActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadCart()
    }

    private fun loadCart() {
        val list = db.getCart()
        b.rvKeranjang.adapter = CartAdapter(requireContext(), list) {
            updateTotal()
        }
        updateTotal()
    }

    private fun updateTotal() {
        b.txtTotal.text = "Total: ${formatRupiah(db.getTotal())}"
    }

    private fun formatRupiah(value: Double): String {
        return NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(value)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}