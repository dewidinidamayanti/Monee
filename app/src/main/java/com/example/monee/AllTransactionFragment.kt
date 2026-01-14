package com.example.monee

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.monee.databinding.FragmentAllTransactionBinding
import com.example.monee.db.Transaksi
import com.example.monee.db.TransaksiViewModel
import com.google.android.material.snackbar.Snackbar

class AllTransactionFragment : Fragment() {

    private var _binding: FragmentAllTransactionBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: TransaksiViewModel
    private lateinit var adapter: TransaksiAdapter

    private var allList = listOf<Transaksi>()
    private var currentFilter = "Semua"
    private var isSortNewest = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[TransaksiViewModel::class.java]

        adapter = TransaksiAdapter(
            onEditClick = { transaksi ->
                val bundle = Bundle().apply { putParcelable("transaksi_obj", transaksi) }
                findNavController().navigate(R.id.action_allTransactionFragment_to_editTransactionFragment, bundle)
            },
            onDeleteClick = { transaksi -> confirmDelete(transaksi) }
        )

        binding.rvAllTransaksi.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAllTransaksi.adapter = adapter

        binding.pbAll.visibility = View.VISIBLE
        binding.rvAllTransaksi.visibility = View.GONE

        viewModel.allTransaksi.observe(viewLifecycleOwner) {
            binding.pbAll.visibility = View.GONE
            binding.rvAllTransaksi.visibility = View.VISIBLE

            allList = it
            applyFilterAndSearch(binding.etSearch.text.toString())
        }

        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: R.id.chip_all
            currentFilter = when (checkedId) {
                R.id.chip_income -> "Pemasukan"
                R.id.chip_expense -> "Pengeluaran"
                else -> "Semua"
            }
            applyFilterAndSearch(binding.etSearch.text.toString())
        }

        binding.btnSort.setOnClickListener {
            isSortNewest = !isSortNewest
            val rotationAngle = if (isSortNewest) 0f else 180f
            binding.btnSort.animate().rotation(rotationAngle).setDuration(300).start()
            applyFilterAndSearch(binding.etSearch.text.toString())
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilterAndSearch(s.toString())
            }
        })

        binding.chipAll.isChecked = true
    }

    private fun confirmDelete(transaksi: Transaksi) {
        // Poin: Validasi konfirmasi sebelum hapus
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.hapus_transaksi_title)
            .setMessage(R.string.hapus_transaksi_msg)
            .setPositiveButton(R.string.hapus) { _, _ ->
                viewModel.delete(transaksi)
                Snackbar.make(binding.root, "Transaksi berhasil dihapus", Snackbar.LENGTH_LONG)
                    .setAction("Undo") {
                        viewModel.insert(transaksi)
                    }.show()
            }
            .setNegativeButton(R.string.batal, null)
            .show()
    }

    private fun applyFilterAndSearch(keyword: String) {
        var result = allList

        if (currentFilter != "Semua") {
            result = result.filter { it.tipe.equals(currentFilter, ignoreCase = true) }
        }

        if (keyword.isNotBlank()) {
            result = result.filter {
                it.judul.contains(keyword, true) ||
                        it.kategori.contains(keyword, true) ||
                        (it.deskripsi?.contains(keyword, true) ?: false)
            }
        }

        result = if (isSortNewest) {
            result.sortedByDescending { it.tanggal }
        } else {
            result.sortedBy { it.tanggal }
        }

        binding.tvCount.text = getString(R.string.jumlah_transaksi, result.size)
        binding.tvEmpty.visibility = if (result.isEmpty()) View.VISIBLE else View.GONE

        adapter.submitList(result)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
