package com.nesia.libraryapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LibraryAdapter(
    private val list: List<Peminjaman>,
    private val onClick: (Peminjaman) -> Unit,
    private val onLongClick: (Peminjaman) -> Unit
) : RecyclerView.Adapter<LibraryAdapter.ViewHolder>() {

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tvJudul: TextView = v.findViewById(R.id.tvItemJudul)
        val tvSub: TextView = v.findViewById(R.id.tvItemSub)
        val tvStatus: TextView = v.findViewById(R.id.tvItemStatus)
        val tvInfo: TextView = v.findViewById(R.id.tvItemInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_library, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.tvJudul.text = item.judulBuku
        holder.tvSub.text = item.namaAnggota
        holder.tvStatus.text = item.status
        holder.tvInfo.text = "${item.tglPinjam} s/d ${item.tglKembali ?: "-"}"

        if (item.status == "dipinjam") {
            holder.tvStatus.setTextColor(android.graphics.Color.RED)
        } else {
            holder.tvStatus.setTextColor(android.graphics.Color.GREEN)
        }

        holder.itemView.setOnClickListener { onClick(item) }
        holder.itemView.setOnLongClickListener {
            onLongClick(item)
            true
        }
    }

    override fun getItemCount(): Int = list.size
}