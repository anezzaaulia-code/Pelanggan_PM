package anezza.aulia.pelanggan_pm.helper

import com.android.volley.AuthFailureError
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.charset.Charset

open class VolleyMultipartRequest(
    method: Int,
    url: String,
    private val listener: Response.Listener<String>,
    errorListener: Response.ErrorListener
) : Request<String>(method, url, errorListener) {

    private val boundary = "apiclient-${System.currentTimeMillis()}"
    private val lineEnd = "\r\n"
    private val twoHyphens = "--"

    data class DataPart(
        val fileName: String,
        val content: ByteArray,
        val type: String
    )

    override fun getBodyContentType(): String {
        return "multipart/form-data; boundary=$boundary"
    }

    @Throws(AuthFailureError::class)
    open fun getByteData(): MutableMap<String, DataPart> {
        return hashMapOf()
    }

    @Throws(AuthFailureError::class)
    override fun getParams(): MutableMap<String, String> {
        return hashMapOf()
    }

    @Throws(AuthFailureError::class)
    override fun getBody(): ByteArray {
        val bos = ByteArrayOutputStream()

        try {
            val textParams = getParams()
            val fileParams = getByteData()

            for ((key, value) in textParams) {
                buildTextPart(bos, key, value)
            }

            for ((key, value) in fileParams) {
                buildFilePart(bos, key, value)
            }

            bos.write((twoHyphens + boundary + twoHyphens + lineEnd).toByteArray())
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return bos.toByteArray()
    }

    @Throws(IOException::class)
    private fun buildTextPart(
        bos: ByteArrayOutputStream,
        key: String,
        value: String
    ) {
        bos.write((twoHyphens + boundary + lineEnd).toByteArray())
        bos.write("Content-Disposition: form-data; name=\"$key\"$lineEnd".toByteArray())
        bos.write("Content-Type: text/plain; charset=UTF-8$lineEnd".toByteArray())
        bos.write(lineEnd.toByteArray())
        bos.write(value.toByteArray(Charset.forName("UTF-8")))
        bos.write(lineEnd.toByteArray())
    }

    @Throws(IOException::class)
    private fun buildFilePart(
        bos: ByteArrayOutputStream,
        key: String,
        dataPart: DataPart
    ) {
        bos.write((twoHyphens + boundary + lineEnd).toByteArray())
        bos.write(
            "Content-Disposition: form-data; name=\"$key\"; filename=\"${dataPart.fileName}\"$lineEnd"
                .toByteArray()
        )
        bos.write("Content-Type: ${dataPart.type}$lineEnd".toByteArray())
        bos.write(lineEnd.toByteArray())
        bos.write(dataPart.content)
        bos.write(lineEnd.toByteArray())
    }

    override fun parseNetworkResponse(response: NetworkResponse): Response<String> {
        val parsed = try {
            String(
                response.data,
                Charset.forName(HttpHeaderParser.parseCharset(response.headers, "UTF-8"))
            )
        } catch (e: Exception) {
            String(response.data)
        }

        return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response))
    }

    override fun deliverResponse(response: String) {
        listener.onResponse(response)
    }
}