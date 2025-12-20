package com.example.monee

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.monee.db.Transaksi
import com.example.monee.db.TransaksiViewModel
import androidx.navigation.fragment.findNavController

class HomeFragment : Fragment() {

    private lateinit var viewModel: TransaksiViewModel
    private lateinit var transaksiAdapter: TransaksiAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[TransaksiViewModel::class.java]

        val tvTotalSaldo = view.findViewById<TextView>(R.id.tvTotalSaldo)
        val tvTotalPemasukan = view.findViewById<TextView>(R.id.tvTotalPemasukan)
        val tvTotalPengeluaran = view.findViewById<TextView>(R.id.tvTotalPengeluaran)
        val tvMiniIncome = view.findViewById<TextView>(R.id.tvMiniIncome)
        val tvMiniExpense = view.findViewById<TextView>(R.id.tvMiniExpense)

        val rvTransaksi = view.findViewById<RecyclerView>(R.id.rvTransaksiTerkini)

        transaksiAdapter = TransaksiAdapter(
            onEditClick = { transaksi ->
                val bundle = Bundle().apply {
                    putInt("transaksiId", transaksi.id)
                }
                findNavController().navigate(
                    R.id.editTransactionFragment,
                    bundle
                )
            },
            onDeleteClick = { transaksi -> confirmDelete(transaksi) }
        )

        rvTransaksi.layoutManager = LinearLayoutManager(requireContext())
        rvTransaksi.adapter = transaksiAdapter
        rvTransaksi.isNestedScrollingEnabled = false

        viewModel.getAllTransaksi().observe(viewLifecycleOwner) { list ->
            if (list.isNullOrEmpty()) {
                tvTotalSaldo.text = "Rp0"
                tvTotalPemasukan.text = "Rp0"
                tvTotalPengeluaran.text = "Rp0"
                tvMiniIncome.text = "Rp0"
                tvMiniExpense.text = "Rp0"
                transaksiAdapter.submitList(emptyList())
                return@observe
            }

            val totalIncome = list.filter { it.tipe == "income" }.sumOf { it.nominal }
            val totalExpense = list.filter { it.tipe == "expense" }.sumOf { it.nominal }
            val saldo = totalIncome - totalExpense

            tvTotalSaldo.text = formatRupiah(saldo)
            tvTotalPemasukan.text = formatRupiah(totalIncome)
            tvTotalPengeluaran.text = formatRupiah(totalExpense)
            tvMiniIncome.text = formatRupiah(totalIncome)
            tvMiniExpense.text = formatRupiah(totalExpense)

            transaksiAdapter.submitList(
                list.sortedByDescending { it.tanggal }.take(3)
            )
        }
    }

    private fun confirmDelete(transaksi: Transaksi) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Hapus Transaksi")
            .setMessage("Yakin ingin menghapus transaksi ini?")
            .setPositiveButton("Hapus") { _, _ ->
                viewModel.delete(transaksi)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun openEdit(transaksi: Transaksi) {
        // nanti sambungkan ke fragment edit
    }

    private fun formatRupiah(amount: Double): String {
        return "Rp" + String.format("%,.0f", amount)
    }
}
