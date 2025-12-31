package com.example.monee

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.monee.db.Transaksi
import com.example.monee.db.TransaksiViewModel

class HomeFragment : Fragment() {

    private lateinit var viewModel: TransaksiViewModel
    private lateinit var transaksiAdapter: TransaksiAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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
        val tvHomeEmpty = view.findViewById<TextView>(R.id.tvHomeEmpty)
        val tvLihatSemua = view.findViewById<TextView>(R.id.tvLihatSemua)
        val rvTransaksi = view.findViewById<RecyclerView>(R.id.rvTransaksiTerkini)

        transaksiAdapter = TransaksiAdapter(
            onEditClick = { transaksi ->
                val bundle = Bundle().apply { putInt("transaksiId", transaksi.id) }
                findNavController().navigate(R.id.action_homeFragment_to_editTransactionFragment, bundle)
            },
            onDeleteClick = { transaksi -> confirmDelete(transaksi) }
        )

        rvTransaksi.layoutManager = LinearLayoutManager(requireContext())
        rvTransaksi.adapter = transaksiAdapter
        rvTransaksi.isNestedScrollingEnabled = false

        tvLihatSemua.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_allTransactionFragment)
        }

        viewModel.allTransaksi.observe(viewLifecycleOwner) { list ->

            if (list.isNullOrEmpty()) {
                tvTotalSaldo.text = "Rp0"
                tvTotalPemasukan.text = "Rp0"
                tvTotalPengeluaran.text = "Rp0"
                tvMiniIncome.text = "Rp0"
                tvMiniExpense.text = "Rp0"
                transaksiAdapter.submitList(emptyList())
                tvHomeEmpty.visibility = View.VISIBLE
                return@observe
            }

            tvHomeEmpty.visibility = View.GONE

            val totalIncome = list.filter { it.tipe == "Pemasukan" }.sumOf { it.nominal }
            val totalExpense = list.filter { it.tipe == "Pengeluaran" }.sumOf { it.nominal }
            val saldo = totalIncome - totalExpense

            tvTotalSaldo.text = formatRupiah(saldo)
            tvTotalPemasukan.text = formatRupiah(totalIncome)
            tvTotalPengeluaran.text = formatRupiah(totalExpense)
            tvMiniIncome.text = formatRupiah(totalIncome)
            tvMiniExpense.text = formatRupiah(totalExpense)

            transaksiAdapter.submitList(list.sortedByDescending { it.tanggal }.take(3))
        }
    }

    private fun confirmDelete(transaksi: Transaksi) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Transaksi")
            .setMessage("Yakin ingin menghapus transaksi ini?")
            .setPositiveButton("Hapus") { _, _ -> viewModel.delete(transaksi) }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun formatRupiah(amount: Double): String {
        return "Rp" + String.format("%,.0f", amount)
    }
}
