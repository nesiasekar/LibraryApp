package com.nesia.libraryapp

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    private lateinit var db: DatabaseHelper
    private lateinit var adapter: LibraryAdapter
    private val dataList = mutableListOf<Peminjaman>()
    private var modeAktif = "LAPORAN"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = DatabaseHelper(this)
        setupRecyclerView()

        findViewById<Button>(R.id.btnNavBuku).setOnClickListener { switchView("BUKU") }
        findViewById<Button>(R.id.btnNavAnggota).setOnClickListener { switchView("ANGGOTA") }
        findViewById<Button>(R.id.btnNavLaporan).setOnClickListener { switchView("LAPORAN") }

        findViewById<FloatingActionButton>(R.id.fabAction).setOnClickListener {
            val menu = arrayOf("Tambah Buku", "Registrasi Anggota", "Pinjam Buku (Transaksi)")
            AlertDialog.Builder(this).setItems(menu) { _, i ->
                when(i) {
                    0 -> showAddBukuDialog()
                    1 -> showAddAnggotaDialog()
                    2 -> showPinjamDialog()
                }
            }.show()
        }
        refreshData()
    }

    private fun switchView(mode: String) {
        modeAktif = mode
        findViewById<TextView>(R.id.tvLabelList).text = "Daftar $mode"
        refreshData()
    }

    private fun refreshData() {
        val stats = db.getDashboardStats()
        if (stats.moveToFirst()) {
            findViewById<TextView>(R.id.tvStatistik).text =
                "Buku: ${stats.getInt(0)} | Anggota: ${stats.getInt(1)} | Dipinjam: ${stats.getInt(2)}"
        }
        stats.close()

        dataList.clear()
        when (modeAktif) {
            "BUKU" -> {
                val c = db.readableDatabase.rawQuery("SELECT * FROM buku", null)
                while (c.moveToNext()) {
                    dataList.add(Peminjaman(c.getInt(0), c.getString(1), "Oleh: ${c.getString(2)}", "Stok: ${c.getInt(5)}", "-", "-"))
                }
                c.close()
            }
            "ANGGOTA" -> {
                val c = db.readableDatabase.rawQuery("SELECT * FROM anggota", null)
                while (c.moveToNext()) {
                    dataList.add(Peminjaman(c.getInt(0), c.getString(1), c.getString(2), "Aktif", "-", "-"))
                }
                c.close()
            }
            "LAPORAN" -> {
                val c = db.getLaporanJoin()
                while (c.moveToNext()) {
                    dataList.add(Peminjaman(c.getInt(0), c.getString(1), "Peminjam: ${c.getString(2)}", c.getString(3), c.getString(4), c.getString(5)))
                }
                c.close()
            }
        }
        adapter.notifyDataSetChanged()
    }

    private fun showAddBukuDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_input, null)
        val et1 = view.findViewById<EditText>(R.id.etInput1).apply { hint = "Judul Buku" }
        val et2 = view.findViewById<EditText>(R.id.etInput2).apply { hint = "Pengarang" }
        val et3 = view.findViewById<EditText>(R.id.etInput3).apply { hint = "Stok" }

        AlertDialog.Builder(this).setTitle("Tambah Buku").setView(view).setPositiveButton("Simpan") { _, _ ->
            db.insertBuku(Buku(
                judul = et1.text.toString(),
                pengarang = et2.text.toString(),
                isbn = "-",
                tahun = 0,
                stok = et3.text.toString().toIntOrNull() ?: 0
            ))
            refreshData()
        }.show()
    }

    private fun showAddAnggotaDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_input, null)
        val et1 = view.findViewById<EditText>(R.id.etInput1).apply { hint = "Nama Lengkap" }
        val et2 = view.findViewById<EditText>(R.id.etInput2).apply { hint = "Email" }
        val et3 = view.findViewById<EditText>(R.id.etInput3).apply { hint = "Nomor HP" }

        AlertDialog.Builder(this).setTitle("Registrasi Anggota").setView(view).setPositiveButton("Daftar") { _, _ ->
            db.insertAnggota(Anggota(nama = et1.text.toString(), email = et2.text.toString(), noHp = et3.text.toString()))
            refreshData()
        }.show()
    }

    private fun showPinjamDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_input, null)
        val et1 = view.findViewById<EditText>(R.id.etInput1).apply { hint = "Judul Buku" }
        val et2 = view.findViewById<EditText>(R.id.etInput2).apply { hint = "Nama Pengarang" }
        val et3 = view.findViewById<EditText>(R.id.etInput3).apply { hint = "Tgl Pinjam (YYYY-MM-DD)" }

        view.findViewById<View>(R.id.layoutInput4).visibility = View.VISIBLE
        val et4 = view.findViewById<EditText>(R.id.etInput4).apply { hint = "Tgl Kembali (YYYY-MM-DD)" }

        AlertDialog.Builder(this).setTitle("Pinjam Buku").setView(view).setPositiveButton("Proses") { _, _ ->
            db.prosesPinjamManual(et1.text.toString(), et2.text.toString(), et3.text.toString(), et4.text.toString())
            refreshData()
        }.show()
    }

    private fun setupRecyclerView() {
        val rv = findViewById<RecyclerView>(R.id.rvUtama)
        rv.layoutManager = LinearLayoutManager(this)

        adapter = LibraryAdapter(dataList, { item ->
            if (modeAktif == "LAPORAN" && item.status == "dipinjam") {
                AlertDialog.Builder(this).setMessage("Kembalikan buku ini?").setPositiveButton("Ya") { _, _ ->
                    db.prosesKembali(item.id)
                    refreshData()
                }.show()
            }
        }, { item ->
            AlertDialog.Builder(this).setMessage("Hapus data ini?").setPositiveButton("Hapus") { _, _ ->
                val tabel = if(modeAktif == "BUKU") "buku" else "anggota"
                db.writableDatabase.delete(tabel, "id=?", arrayOf(item.id.toString()))
                refreshData()
            }.show()
        })
        rv.adapter = adapter
    }
}