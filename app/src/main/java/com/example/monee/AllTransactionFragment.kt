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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.monee.db.Transaksi
import com.example.monee.db.TransaksiViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

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
        val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroupFilter)
        val chipAll = view.findViewById<Chip>(R.id.chipAll)
        val btnSort = view.findViewById<ImageView>(R.id.btnSort)
        val rv = view.findViewById<RecyclerView>(R.id.rvAllTransaksi)
        val tvCount = view.findViewById<TextView>(R.id.tvCount)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmpty)

        adapter = TransaksiAdapter(
            onEditClick = { transaksi ->
                val bundle = Bundle().apply { putInt("transaksiId", transaksi.id) }
                findNavController().navigate(R.id.action_allTransactionFragment_to_editTransactionFragment, bundle)
            },
            onDeleteClick = { transaksi ->
                viewModel.delete(transaksi)
            }
        )

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        viewModel.allTransaksi.observe(viewLifecycleOwner) {
            allList = it
            applyFilterAndSearch(etSearch.text.toString(), tvCount, tvEmpty)
        }

        chipGroup.setOnCheckedChangeListener { _, checkedId ->
            currentFilter = when (checkedId) {
                R.id.chipIncome -> "Pemasukan"
                R.id.chipExpense -> "Pengeluaran"
                else -> "Semua"
            }
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

        chipAll.isChecked = true
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
}
