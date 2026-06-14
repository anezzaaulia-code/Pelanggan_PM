package anezza.aulia.pelanggan_pm.helper

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import anezza.aulia.pelanggan_pm.model.CartItem

class CartDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "si_tahu_cart.db"
        private const val DATABASE_VERSION = 3

        private const val TABLE_CART = "cart_items"

        private const val COL_ID = "id"
        private const val COL_PRODUK_ID = "produk_id"
        private const val COL_NAMA = "nama"
        private const val COL_HARGA = "harga"
        private const val COL_STOK = "stok"
        private const val COL_SATUAN = "satuan"
        private const val COL_GAMBAR = "gambar"
        private const val COL_JUMLAH = "jumlah"
        private const val COL_CREATED_AT = "created_at"
        private const val COL_UPDATED_AT = "updated_at"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_CART (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_PRODUK_ID INTEGER NOT NULL,
                $COL_NAMA TEXT NOT NULL,
                $COL_HARGA REAL NOT NULL DEFAULT 0,
                $COL_STOK INTEGER NOT NULL DEFAULT 0,
                $COL_SATUAN TEXT,
                $COL_GAMBAR TEXT,
                $COL_JUMLAH INTEGER NOT NULL DEFAULT 1,
                $COL_CREATED_AT TEXT DEFAULT CURRENT_TIMESTAMP,
                $COL_UPDATED_AT TEXT DEFAULT CURRENT_TIMESTAMP
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE UNIQUE INDEX idx_cart_produk_id
            ON $TABLE_CART ($COL_PRODUK_ID)
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS cart")
        db.execSQL("DROP TABLE IF EXISTS item_cart")
        db.execSQL("DROP TABLE IF EXISTS keranjang")
        db.execSQL("DROP TABLE IF EXISTS item_keranjang")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CART")
        onCreate(db)
    }

    fun addToCart(item: CartItem) {
        if (item.produkId <= 0) {
            return
        }

        if (item.stok <= 0) {
            return
        }

        val db = writableDatabase

        val cursor = db.rawQuery(
            "SELECT $COL_JUMLAH FROM $TABLE_CART WHERE $COL_PRODUK_ID = ?",
            arrayOf(item.produkId.toString())
        )

        if (cursor.moveToFirst()) {
            val jumlahLama = cursor.getInt(0)

            val jumlahBaru = (jumlahLama + item.jumlah)
                .coerceAtLeast(1)
                .coerceAtMost(item.stok)

            db.execSQL(
                """
                UPDATE $TABLE_CART
                SET $COL_JUMLAH = ?,
                    $COL_HARGA = ?,
                    $COL_STOK = ?,
                    $COL_NAMA = ?,
                    $COL_SATUAN = ?,
                    $COL_GAMBAR = ?,
                    $COL_UPDATED_AT = CURRENT_TIMESTAMP
                WHERE $COL_PRODUK_ID = ?
                """.trimIndent(),
                arrayOf(
                    jumlahBaru,
                    item.harga,
                    item.stok,
                    item.nama,
                    item.satuan,
                    item.gambar,
                    item.produkId
                )
            )
        } else {
            val jumlahAwal = item.jumlah
                .coerceAtLeast(1)
                .coerceAtMost(item.stok)

            val values = ContentValues().apply {
                put(COL_PRODUK_ID, item.produkId)
                put(COL_NAMA, item.nama)
                put(COL_HARGA, item.harga)
                put(COL_STOK, item.stok)
                put(COL_SATUAN, item.satuan)
                put(COL_GAMBAR, item.gambar)
                put(COL_JUMLAH, jumlahAwal)
            }

            db.insert(TABLE_CART, null, values)
        }

        cursor.close()
        db.close()
    }

    fun getCart(): ArrayList<CartItem> {
        val list = ArrayList<CartItem>()
        val db = readableDatabase

        val cursor = db.rawQuery(
            """
            SELECT 
                $COL_ID,
                $COL_PRODUK_ID,
                $COL_NAMA,
                $COL_HARGA,
                $COL_STOK,
                $COL_SATUAN,
                $COL_GAMBAR,
                $COL_JUMLAH
            FROM $TABLE_CART
            ORDER BY $COL_ID DESC
            """.trimIndent(),
            null
        )

        while (cursor.moveToNext()) {
            list.add(
                CartItem(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                    produkId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PRODUK_ID)),
                    nama = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAMA)),
                    harga = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_HARGA)),
                    stok = cursor.getInt(cursor.getColumnIndexOrThrow(COL_STOK)),
                    satuan = cursor.getString(cursor.getColumnIndexOrThrow(COL_SATUAN)) ?: "",
                    gambar = cursor.getString(cursor.getColumnIndexOrThrow(COL_GAMBAR)),
                    jumlah = cursor.getInt(cursor.getColumnIndexOrThrow(COL_JUMLAH))
                )
            )
        }

        cursor.close()
        db.close()

        return list
    }

    fun updateJumlah(id: Int, jumlah: Int) {
        if (id <= 0) {
            return
        }

        if (jumlah <= 0) {
            deleteItem(id)
            return
        }

        val db = writableDatabase

        val cursor = db.rawQuery(
            "SELECT $COL_STOK FROM $TABLE_CART WHERE $COL_ID = ?",
            arrayOf(id.toString())
        )

        var stok = jumlah

        if (cursor.moveToFirst()) {
            stok = cursor.getInt(0)
        }

        cursor.close()

        val jumlahAkhir = jumlah
            .coerceAtLeast(1)
            .coerceAtMost(stok)

        db.execSQL(
            """
            UPDATE $TABLE_CART
            SET $COL_JUMLAH = ?,
                $COL_UPDATED_AT = CURRENT_TIMESTAMP
            WHERE $COL_ID = ?
            """.trimIndent(),
            arrayOf(jumlahAkhir, id)
        )

        db.close()
    }

    fun deleteItem(id: Int) {
        if (id <= 0) {
            return
        }

        val db = writableDatabase

        db.execSQL(
            "DELETE FROM $TABLE_CART WHERE $COL_ID = ?",
            arrayOf(id)
        )

        db.close()
    }

    fun clearCart() {
        val db = writableDatabase
        db.execSQL("DELETE FROM $TABLE_CART")
        db.close()
    }

    fun getTotal(): Double {
        var total = 0.0
        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT $COL_HARGA, $COL_JUMLAH FROM $TABLE_CART",
            null
        )

        while (cursor.moveToNext()) {
            val harga = cursor.getDouble(0)
            val jumlah = cursor.getInt(1)
            total += harga * jumlah
        }

        cursor.close()
        db.close()

        return total
    }

    fun getJumlahItem(): Int {
        var totalItem = 0
        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT COALESCE(SUM($COL_JUMLAH), 0) FROM $TABLE_CART",
            null
        )

        if (cursor.moveToFirst()) {
            totalItem = cursor.getInt(0)
        }

        cursor.close()
        db.close()

        return totalItem
    }

    fun isCartEmpty(): Boolean {
        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_CART",
            null
        )

        var count = 0

        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }

        cursor.close()
        db.close()

        return count == 0
    }
}