package com.nesia.libraryapp

data class Buku(
    val id: Int = 0,
    val judul: String,
    val pengarang: String,
    val isbn: String,
    val tahun: Int,
    val stok: Int
)
data class Anggota(
    val id: Int=0,
    val nama: String,
    val email: String,
    val noHp: String,
    val tglDaftar: String = ""
)

data class Peminjaman(
    val id: Int,
    val judulBuku: String,
    val namaAnggota: String,
    val status: String,
    val tglPinjam: String,
    val tglKembali: String
)