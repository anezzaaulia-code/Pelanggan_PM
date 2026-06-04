package anezza.aulia.pelanggan_pm.helper


import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import anezza.aulia.pelanggan_pm.model.CartItem

class CartDbHelper(context: Context) : SQLiteOpenHelper(context, "si_tahu_cart.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE cart (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                produk_id INTEGER,
                nama TEXT,
                harga REAL,
                stok INTEGER,
                satuan TEXT,
                gambar TEXT,
                jumlah INTEGER
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS cart")
        onCreate(db)
    }

    fun addToCart(item: CartItem) {
        val db = writableDatabase

        val cursor = db.rawQuery(
            "SELECT jumlah FROM cart WHERE produk_id = ?",
            arrayOf(item.produkId.toString())
        )

        if (cursor.moveToFirst()) {
            val jumlahLama = cursor.getInt(0)
            val jumlahBaru = jumlahLama + item.jumlah

            db.execSQL(
                "UPDATE cart SET jumlah = ? WHERE produk_id = ?",
                arrayOf(jumlahBaru, item.produkId)
            )
        } else {
            val cv = ContentValues()
            cv.put("produk_id", item.produkId)
            cv.put("nama", item.nama)
            cv.put("harga", item.harga)
            cv.put("stok", item.stok)
            cv.put("satuan", item.satuan)
            cv.put("gambar", item.gambar)
            cv.put("jumlah", item.jumlah)
            db.insert("cart", null, cv)
        }

        cursor.close()
        db.close()
    }

    fun getCart(): ArrayList<CartItem> {
        val list = ArrayList<CartItem>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM cart", null)

        while (cursor.moveToNext()) {
            list.add(
                CartItem(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    produkId = cursor.getInt(cursor.getColumnIndexOrThrow("produk_id")),
                    nama = cursor.getString(cursor.getColumnIndexOrThrow("nama")),
                    harga = cursor.getDouble(cursor.getColumnIndexOrThrow("harga")),
                    stok = cursor.getInt(cursor.getColumnIndexOrThrow("stok")),
                    satuan = cursor.getString(cursor.getColumnIndexOrThrow("satuan")),
                    gambar = cursor.getString(cursor.getColumnIndexOrThrow("gambar")),
                    jumlah = cursor.getInt(cursor.getColumnIndexOrThrow("jumlah"))
                )
            )
        }

        cursor.close()
        db.close()
        return list
    }

    fun updateJumlah(id: Int, jumlah: Int) {
        val db = writableDatabase
        db.execSQL("UPDATE cart SET jumlah = ? WHERE id = ?", arrayOf(jumlah, id))
        db.close()
    }

    fun deleteItem(id: Int) {
        val db = writableDatabase
        db.execSQL("DELETE FROM cart WHERE id = ?", arrayOf(id))
        db.close()
    }

    fun clearCart() {
        val db = writableDatabase
        db.execSQL("DELETE FROM cart")
        db.close()
    }

    fun getTotal(): Double {
        var total = 0.0
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT harga, jumlah FROM cart", null)

        while (cursor.moveToNext()) {
            val harga = cursor.getDouble(0)
            val jumlah = cursor.getInt(1)
            total += harga * jumlah
        }

        cursor.close()
        db.close()
        return total
    }
}