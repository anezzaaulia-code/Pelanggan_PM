package anezza.aulia.pelanggan_pm

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import anezza.aulia.pelanggan_pm.adapter.AlamatAdapter
import anezza.aulia.pelanggan_pm.databinding.ActivityAlamatBinding
import anezza.aulia.pelanggan_pm.helper.ApiConfig
import anezza.aulia.pelanggan_pm.helper.SessionManager
import anezza.aulia.pelanggan_pm.model.Alamat
import com.android.volley.AuthFailureError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

class AlamatActivity : AppCompatActivity() {

    private lateinit var b: ActivityAlamatBinding
    private lateinit var session: SessionManager

    private val listAlamat = ArrayList<Alamat>()

    private var latitude: Double? = null
    private var longitude: Double? = null
    private var marker: Marker? = null

    private val requestLocationCode = 201

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = Color.parseColor("#FFF7E6")
        window.navigationBarColor = Color.parseColor("#FFFDF7")
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true

        Configuration.getInstance().load(
            applicationContext,
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )

        b = ActivityAlamatBinding.inflate(layoutInflater)
        setContentView(b.root)

        session = SessionManager(this)

        setupMap()
        setupRecycler()
        loadAlamat()

        b.btnLokasiSaya.setOnClickListener {
            ambilLokasiSaatIni()
        }

        b.btnSimpanAlamat.setOnClickListener {
            simpanAlamat()
        }
    }

    private fun setupMap() {
        b.mapView.setTileSource(TileSourceFactory.MAPNIK)
        b.mapView.setMultiTouchControls(true)

        val kediri = GeoPoint(-7.8166, 112.0119)
        b.mapView.controller.setZoom(16.0)
        b.mapView.controller.setCenter(kediri)

        tambahMarker(kediri.latitude, kediri.longitude)

        b.mapView.overlays.add(
            object : org.osmdroid.views.overlay.Overlay() {
                override fun onSingleTapConfirmed(
                    e: android.view.MotionEvent?,
                    mapView: org.osmdroid.views.MapView?
                ): Boolean {
                    if (e != null && mapView != null) {
                        val point = mapView.projection.fromPixels(
                            e.x.toInt(),
                            e.y.toInt()
                        ) as GeoPoint

                        tambahMarker(point.latitude, point.longitude)
                        return true
                    }

                    return false
                }
            }
        )
    }

    private fun tambahMarker(lat: Double, lon: Double) {
        latitude = lat
        longitude = lon

        val point = GeoPoint(lat, lon)

        if (marker == null) {
            marker = Marker(b.mapView)
            marker?.title = "Lokasi alamat"
            marker?.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            b.mapView.overlays.add(marker)
        }

        marker?.position = point
        b.mapView.controller.setCenter(point)
        b.mapView.invalidate()

        b.txtKoordinat.text = "Latitude: $lat | Longitude: $lon"
    }

    private fun setupRecycler() {
        b.rvAlamat.layoutManager = LinearLayoutManager(this)
    }

    private fun loadAlamat() {
        val request = object : StringRequest(
            Method.GET,
            ApiConfig.ADDRESSES,
            { response ->
                listAlamat.clear()

                val obj = JSONObject(response)
                val arr = obj.getJSONArray("data")

                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)

                    listAlamat.add(
                        Alamat(
                            id = o.getInt("id"),
                            namaPenerima = o.optString("nama_penerima", ""),
                            telepon = o.optString("telepon", ""),
                            alamatLengkap = o.optString("alamat_lengkap", ""),
                            latitude = if (o.isNull("latitude")) null else o.optDouble("latitude"),
                            longitude = if (o.isNull("longitude")) null else o.optDouble("longitude"),
                            utama = o.optBoolean("utama", false)
                        )
                    )
                }

                b.rvAlamat.adapter = AlamatAdapter(
                    this,
                    listAlamat,
                    onSetUtama = { alamat -> setAlamatUtama(alamat.id) },
                    onHapus = { alamat -> hapusAlamat(alamat.id) }
                )
            },
            { error ->
                Toast.makeText(
                    this,
                    bacaError(error, "Gagal mengambil alamat"),
                    Toast.LENGTH_LONG
                ).show()
            }
        ) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): MutableMap<String, String> {
                return headerAuth()
            }
        }

        Volley.newRequestQueue(this).add(request)
    }

    private fun simpanAlamat() {
        val namaPenerima = b.edtNamaPenerima.text.toString().trim()
        val telepon = b.edtTelepon.text.toString().trim()
        val alamatLengkap = b.edtAlamatLengkap.text.toString().trim()

        if (namaPenerima.isEmpty()) {
            b.edtNamaPenerima.error = "Nama penerima wajib diisi"
            b.edtNamaPenerima.requestFocus()
            return
        }

        if (telepon.isEmpty()) {
            b.edtTelepon.error = "Nomor HP wajib diisi"
            b.edtTelepon.requestFocus()
            return
        }

        if (alamatLengkap.isEmpty()) {
            b.edtAlamatLengkap.error = "Alamat lengkap wajib diisi"
            b.edtAlamatLengkap.requestFocus()
            return
        }

        val request = object : StringRequest(
            Method.POST,
            ApiConfig.ADDRESSES,
            { response ->
                val obj = JSONObject(response)

                Toast.makeText(
                    this,
                    obj.optString("message", "Alamat berhasil disimpan"),
                    Toast.LENGTH_SHORT
                ).show()

                b.edtNamaPenerima.text.clear()
                b.edtTelepon.text.clear()
                b.edtAlamatLengkap.text.clear()
                b.cbUtama.isChecked = false

                loadAlamat()
            },
            { error ->
                Toast.makeText(
                    this,
                    bacaError(error, "Gagal menyimpan alamat"),
                    Toast.LENGTH_LONG
                ).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "nama_penerima" to namaPenerima,
                    "telepon" to telepon,
                    "alamat_lengkap" to alamatLengkap,
                    "latitude" to (latitude?.toString() ?: ""),
                    "longitude" to (longitude?.toString() ?: ""),
                    "utama" to if (b.cbUtama.isChecked) "1" else "0"
                )
            }

            @Throws(AuthFailureError::class)
            override fun getHeaders(): MutableMap<String, String> {
                return headerAuth()
            }
        }

        Volley.newRequestQueue(this).add(request)
    }

    private fun setAlamatUtama(id: Int) {
        val request = object : StringRequest(
            Method.PATCH,
            "${ApiConfig.ADDRESSES}/$id/main",
            { response ->
                val obj = JSONObject(response)

                Toast.makeText(
                    this,
                    obj.optString("message", "Alamat utama dipilih"),
                    Toast.LENGTH_SHORT
                ).show()

                loadAlamat()
            },
            { error ->
                Toast.makeText(
                    this,
                    bacaError(error, "Gagal memilih alamat utama"),
                    Toast.LENGTH_LONG
                ).show()
            }
        ) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): MutableMap<String, String> {
                return headerAuth()
            }
        }

        Volley.newRequestQueue(this).add(request)
    }

    private fun hapusAlamat(id: Int) {
        val request = object : StringRequest(
            Method.DELETE,
            "${ApiConfig.ADDRESSES}/$id",
            { response ->
                val obj = JSONObject(response)

                Toast.makeText(
                    this,
                    obj.optString("message", "Alamat dihapus"),
                    Toast.LENGTH_SHORT
                ).show()

                loadAlamat()
            },
            { error ->
                Toast.makeText(
                    this,
                    bacaError(error, "Gagal menghapus alamat"),
                    Toast.LENGTH_LONG
                ).show()
            }
        ) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): MutableMap<String, String> {
                return headerAuth()
            }
        }

        Volley.newRequestQueue(this).add(request)
    }

    private fun ambilLokasiSaatIni() {
        if (
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                requestLocationCode
            )
            return
        }

        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        val lokasiGps: Location? = try {
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
            null
        }

        val lokasiNetwork: Location? = try {
            locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        } catch (e: Exception) {
            null
        }

        val lokasi = lokasiGps ?: lokasiNetwork

        if (lokasi != null) {
            tambahMarker(lokasi.latitude, lokasi.longitude)
            Toast.makeText(this, "Lokasi berhasil diambil", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(
                this,
                "Lokasi belum terbaca. Aktifkan GPS, lalu tap titik alamat di maps.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == requestLocationCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ambilLokasiSaatIni()
            } else {
                Toast.makeText(this, "Izin lokasi ditolak", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun headerAuth(): MutableMap<String, String> {
        return hashMapOf(
            "Authorization" to "Bearer ${session.token()}",
            "Accept" to "application/json"
        )
    }

    private fun bacaError(error: com.android.volley.VolleyError, fallback: String): String {
        return error.networkResponse?.data?.let {
            try {
                JSONObject(String(it)).optString("message", fallback)
            } catch (e: Exception) {
                fallback
            }
        } ?: fallback
    }

    override fun onResume() {
        super.onResume()
        b.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        b.mapView.onPause()
    }
}