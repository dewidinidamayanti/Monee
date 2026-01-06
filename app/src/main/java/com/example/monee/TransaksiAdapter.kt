package com.example.monee

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.monee.db.Transaksi
import java.text.SimpleDateFormat
import java.util.*

class TransaksiAdapter(
    private val onEditClick: (Transaksi) -> Unit,
    private val onDeleteClick: (Transaksi) -> Unit
) : ListAdapter<Transaksi, TransaksiAdapter.VH>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Transaksi>() {
            override fun areItemsTheSame(oldItem: Transaksi, newItem: Transaksi) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Transaksi, newItem: Transaksi) =
                oldItem == newItem
        }
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val ivIcon: ImageView = itemView.findViewById(R.id.ivIcon)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        private val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)

        private val iconBg: View = itemView.findViewById(R.id.iconBg)

        fun bind(item: Transaksi) {

            tvTitle.text = item.judul
            tvCategory.text = item.kategori

            val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
            tvDate.text = sdf.format(Date(item.tanggal))

            if (item.tipe.equals("pemasukan", ignoreCase = true)) {
                tvAmount.text = "+ ${formatRupiah(item.nominal)}"
                tvAmount.setTextColor(itemView.context.getColor(R.color.incomeGreen))

                ivIcon.setColorFilter(itemView.context.getColor(R.color.incomeGreen))
                iconBg.setBackgroundResource(R.drawable.bg_circle_green_soft)

            } else {
                tvAmount.text = "- ${formatRupiah(item.nominal)}"
                tvAmount.setTextColor(itemView.context.getColor(R.color.expenseRed))

                ivIcon.setColorFilter(itemView.context.getColor(R.color.expenseRed))
                iconBg.setBackgroundResource(R.drawable.bg_circle_red_soft)
            }


            ivIcon.setImageResource(getCategoryIcon(item.kategori))

            btnDelete.setOnClickListener { onDeleteClick(item) }
            itemView.setOnClickListener { onEditClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaksi, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    private fun formatRupiah(amount: Double): String {
        return "Rp" + String.format("%,.0f", amount)
    }

    private fun getCategoryIcon(category: String): Int {
        return when (category.lowercase(Locale.getDefault())) {
            "makanan" -> R.drawable.ic_makanan
            "belanja" -> R.drawable.ic_belanja
            "transportasi" -> R.drawable.ic_transportasi
            "utilitas" -> R.drawable.ic_utilitas
            "perumahan" -> R.drawable.ic_perumahan
            "hiburan" -> R.drawable.ic_hiburan
            "kesehatan" -> R.drawable.ic_kesehatan
            else -> R.drawable.ic_lainnya
        }
    }
}
