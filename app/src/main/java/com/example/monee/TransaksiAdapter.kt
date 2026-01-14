package com.example.monee

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.monee.db.Transaksi
import java.text.SimpleDateFormat
import java.util.*
import com.example.monee.databinding.ItemTransaksiBinding

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

    inner class VH(private val binding: ItemTransaksiBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Transaksi) {
            binding.tvTitle.text = item.judul
            binding.tvCategory.text = item.kategori

            val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
            binding.tvDate.text = sdf.format(Date(item.tanggal))

            if (item.tipe.equals("pemasukan", ignoreCase = true)) {
                binding.tvAmount.text = String.format(Locale("id", "ID"), "+ %s", formatRupiah(item.nominal))
                binding.tvAmount.setTextColor(itemView.context.getColor(R.color.incomeGreen))
                binding.ivIcon.setColorFilter(itemView.context.getColor(R.color.incomeGreen))
                binding.flIconBg.setBackgroundResource(R.drawable.bg_circle_green_soft)
            } else {
                binding.tvAmount.text = String.format(Locale("id", "ID"), "- %s", formatRupiah(item.nominal))
                binding.tvAmount.setTextColor(itemView.context.getColor(R.color.expenseRed))
                binding.ivIcon.setColorFilter(itemView.context.getColor(R.color.expenseRed))
                binding.flIconBg.setBackgroundResource(R.drawable.bg_circle_red_soft)
            }

            binding.ivIcon.setImageResource(getCategoryIcon(item.kategori))

            binding.flDeleteBg.setOnClickListener { onDeleteClick(item) }
            itemView.setOnClickListener { onEditClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemTransaksiBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    private fun formatRupiah(amount: Double): String {
        return "Rp" + String.format(Locale("id", "ID"), "%,.0f", amount)
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
            "gaji" -> R.drawable.ic_gaji
            "hadiah" -> R.drawable.ic_hadiah
            else -> R.drawable.ic_lainnya
        }
    }
}
