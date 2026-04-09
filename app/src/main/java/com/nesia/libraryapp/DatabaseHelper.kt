package com.nesia.libraryapp

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "library_db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE buku (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                judul TEXT,
                pengarang TEXT,
                isbn TEXT,
                tahun INTEGER,
                stok INTEGER
            )
        """)

        db.execSQL("""
            CREATE TABLE anggota (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nama TEXT,
                email TEXT,
                no_hp TEXT,
                tgl_daftar TEXT DEFAULT (date('now'))
            )
        """)


        db.execSQL("""
            CREATE TABLE peminjaman (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                buku_id INTEGER,
                anggota_id INTEGER,
                judul_manual TEXT, 
                pengarang_manual TEXT,
                tgl_pinjam TEXT,
                tgl_kembali TEXT,
                status TEXT DEFAULT 'dipinjam',
                FOREIGN KEY(buku_id) REFERENCES buku(id),
                FOREIGN KEY(anggota_id) REFERENCES anggota(id)
            )
        """)
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }


    fun insertBuku(b: Buku): Long {
        val v = ContentValues().apply {
            put("judul", b.judul); put("pengarang", b.pengarang)
            put("isbn", b.isbn); put("tahun", b.tahun); put("stok", b.stok)
        }
        return writableDatabase.insert("buku", null, v)
    }
    fun getAllBuku(): Cursor = readableDatabase.rawQuery("SELECT * FROM buku", null)
    fun updateBuku(b: Buku): Int {
        val v = ContentValues().apply {
            put("judul", b.judul); put("pengarang", b.pengarang); put("stok", b.stok)
        }
        return writableDatabase.update("buku", v, "id = ?", arrayOf(b.id.toString()))
    }
    fun deleteBuku(id: Int): Int = writableDatabase.delete("buku", "id = ?", arrayOf(id.toString()))

    // --- CRUD ANGGOTA ---
    fun insertAnggota(a: Anggota): Long {
        val v = ContentValues().apply {
            put("nama", a.nama); put("email", a.email); put("no_hp", a.noHp)
        }
        return writableDatabase.insert("anggota", null, v)
    }
    fun getAllAnggota(): Cursor = readableDatabase.rawQuery("SELECT * FROM anggota", null)
    fun updateAnggota(a: Anggota): Int {
        val v = ContentValues().apply {
            put("nama", a.nama); put("email", a.email); put("no_hp", a.noHp)
        }
        return writableDatabase.update("anggota", v, "id = ?", arrayOf(a.id.toString()))
    }
    fun deleteAnggota(id: Int): Int = writableDatabase.delete("anggota", "id = ?", arrayOf(id.toString()))


    fun prosesPinjamManual(judul: String, pengarang: String, tglP: String, tglK: String): Boolean {
        val db = writableDatabase
        val v = ContentValues().apply {
            put("judul_manual", judul)
            put("pengarang_manual", pengarang)
            put("tgl_pinjam", tglP)
            put("tgl_kembali", tglK)
            put("status", "dipinjam")
        }
        val hasilPinjam = db.insert("peminjaman", null, v)

        if (hasilPinjam != -1L) {
            db.execSQL("UPDATE buku SET stok = stok - 1 WHERE judul = ? AND stok > 0", arrayOf(judul))
        }

        return hasilPinjam != -1L
    }


    fun prosesKembali(pId: Int): Boolean {
        val v = ContentValues().apply { put("status", "kembali") }
        return writableDatabase.update("peminjaman", v, "id = ?", arrayOf(pId.toString())) > 0
    }


    fun getDashboardStats(): Cursor {
        return readableDatabase.rawQuery("""
            SELECT 
                (SELECT COUNT(*) FROM buku),
                (SELECT COUNT(*) FROM anggota),
                (SELECT COUNT(*) FROM peminjaman WHERE status='dipinjam')
        """, null)
    }

    fun getLaporanJoin(): Cursor {
        return readableDatabase.rawQuery("SELECT id, judul_manual, pengarang_manual, status, tgl_pinjam, tgl_kembali FROM peminjaman ORDER BY id DESC", null)
    }

    override fun onUpgrade(db: SQLiteDatabase, old: Int, new: Int) {
        db.execSQL("DROP TABLE IF EXISTS peminjaman")
        db.execSQL("DROP TABLE IF EXISTS anggota")
        db.execSQL("DROP TABLE IF EXISTS buku")
        onCreate(db)
    }
}