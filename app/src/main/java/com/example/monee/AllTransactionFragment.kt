package com.example.monee

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.monee.db.Transaksi
import com.example.monee.db.TransaksiViewModel
import com.google.android.material.chip.Chip

class AllTransactionFragment : Fragment(R.layout.fragment_all_transaction) {

    private lateinit var viewModel: TransaksiViewModel
    private lateinit var adapter: TransaksiAdapter

    private var allList = listOf<Transaksi>()
    private var currentFilter = "Semua"
    private var isSortNewest = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[TransaksiViewModel::class.java]

        val etSearch = view.findViewById<EditText>(R.id.etSearch)
        val chipAll = view.findViewById<Chip>(R.id.chipAll)
        val chipIncome = view.findViewById<Chip>(R.id.chipIncome)
        val chipExpense = view.findViewById<Chip>(R.id.chipExpense)
        val btnSort = view.findViewById<ImageView>(R.id.btnSort)
        val rv = view.findViewById<RecyclerView>(R.id.rvAllTransaksi)
        val tvCount = view.findViewById<TextView>(R.id.tvCount)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmpty)

        adapter = TransaksiAdapter(
            onEditClick = { transaksi ->
                // TODO: Navigasi ke EditTransactionFragment
            },
            onDeleteClick = { transaksi ->
                viewModel.delete(transaksi)
            }
        )

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        viewModel.getAllTransaksi().observe(viewLifecycleOwner) {
            allList = it
            applyFilterAndSearch(etSearch.text.toString(), tvCount, tvEmpty)
        }

        chipAll.setOnClickListener {
            currentFilter = "Semua"
            updateChipHighlight(chipAll, chipIncome, chipExpense)
            applyFilterAndSearch(etSearch.text.toString(), tvCount, tvEmpty)
        }

        chipIncome.setOnClickListener {
            currentFilter = "Pemasukan"
            updateChipHighlight(chipAll, chipIncome, chipExpense)
            applyFilterAndSearch(etSearch.text.toString(), tvCount, tvEmpty)
        }

        chipExpense.setOnClickListener {
            currentFilter = "Pengeluaran"
            updateChipHighlight(chipAll, chipIncome, chipExpense)
            applyFilterAndSearch(etSearch.text.toString(), tvCount, tvEmpty)
        }

        btnSort.setOnClickListener {
            isSortNewest = !isSortNewest
            applyFilterAndSearch(etSearch.text.toString(), tvCount, tvEmpty)
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilterAndSearch(s.toString(), tvCount, tvEmpty)
            }
        })

        updateChipHighlight(chipAll, chipIncome, chipExpense)
    }

    private fun applyFilterAndSearch(keyword: String, tvCount: TextView, tvEmpty: TextView) {

        var result = allList

        if (currentFilter != "Semua") {
            result = result.filter { it.tipe == currentFilter }
        }

        if (keyword.isNotBlank()) {
            result = result.filter {
                it.judul.contains(keyword, true) ||
                        it.kategori.contains(keyword, true) ||
                        it.deskripsi.contains(keyword, true)
            }
        }

        result = if (isSortNewest) {
            result.sortedByDescending { it.tanggal }
        } else {
            result.sortedBy { it.tanggal }
        }

        tvCount.text = "${result.size} Transaksi"
        tvEmpty.visibility = if (result.isEmpty()) View.VISIBLE else View.GONE

        adapter.submitList(result)
    }

    private fun updateChipHighlight(chipAll: Chip, chipIncome: Chip, chipExpense: Chip) {

        fun style(chip: Chip, active: Boolean) {
            chip.setTextColor(
                if (active)
                    requireContext().getColor(R.color.primaryBlue)
                else
                    requireContext().getColor(R.color.textSecondary)
            )
        }

        style(chipAll, currentFilter == "Semua")
        style(chipIncome, currentFilter == "Pemasukan")
        style(chipExpense, currentFilter == "Pengeluaran")
    }
}
