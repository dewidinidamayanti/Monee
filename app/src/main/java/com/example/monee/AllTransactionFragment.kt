package com.example.monee

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.monee.databinding.FragmentAllTransactionBinding // 1. Impor kelas binding
import com.example.monee.db.Transaksi
import com.example.monee.db.TransaksiViewModel

// Hapus R.layout dari konstruktor Fragment
class AllTransactionFragment : Fragment() {

    // 2. Deklarasikan variabel binding
    private var _binding: FragmentAllTransactionBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: TransaksiViewModel
    private lateinit var adapter: TransaksiAdapter

    private var allList = listOf<Transaksi>()
    private var currentFilter = "Semua"
    private var isSortNewest = true

    // 3. Gunakan onCreateView untuk inflate layout
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

        // 4. Hapus semua `findViewById`, kita akan gunakan `binding`
        // val etSearch = view.findViewById<EditText>(R.id.etSearch)
        // ...dan seterusnya...

        adapter = TransaksiAdapter(
            onEditClick = { transaksi ->
                val bundle = Bundle().apply { putInt("transaksiId", transaksi.id) }
                findNavController().navigate(R.id.action_allTransactionFragment_to_editTransactionFragment, bundle)
            },
            onDeleteClick = { transaksi ->
                viewModel.delete(transaksi)
            }
        )

        // 5. Gunakan objek `binding` untuk mengakses semua view
        binding.rvAllTransaksi.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAllTransaksi.adapter = adapter

        viewModel.allTransaksi.observe(viewLifecycleOwner) {
            allList = it
            applyFilterAndSearch(binding.etSearch.text.toString())
        }

        // Ini sudah kita perbaiki sebelumnya, tetap gunakan setOnCheckedStateChangeListener
        binding.chipGroupFilter.setOnCheckedStateChangeListener { group, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: R.id.chipAll
            currentFilter = when (checkedId) {
                R.id.chipIncome -> "Pemasukan"
                R.id.chipExpense -> "Pengeluaran"
                else -> "Semua"
            }
            applyFilterAndSearch(binding.etSearch.text.toString())
        }

        binding.btnSort.setImageResource(R.drawable.ic_sort) // Gunakan satu ikon saja

        binding.btnSort.setOnClickListener {
            isSortNewest = !isSortNewest

            // Animasikan rotasi ikon
            val rotationAngle = if (isSortNewest) 0f else 180f // 0 derajat untuk terbaru, 180 derajat (terbalik) untuk terlama
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

        // Atur chip "Semua" sebagai default
        binding.chipAll.isChecked = true
    }

    private fun applyFilterAndSearch(keyword: String) {

        var result = allList

        if (currentFilter != "Semua") {
            result = result.filter { it.tipe == currentFilter }
        }

        if (keyword.isNotBlank()) {
            result = result.filter {
                it.judul.contains(keyword, true) ||
                        it.kategori.contains(keyword, true) ||
                        (it.deskripsi?.contains(keyword, true) ?: false) // Lebih aman untuk deskripsi
            }
        }

        result = if (isSortNewest) {
            result.sortedByDescending { it.tanggal }
        } else {
            result.sortedBy { it.tanggal }
        }

        // 6. Gunakan `binding` juga di sini
        binding.tvCount.text = "${result.size} Transaksi"
        binding.tvEmpty.visibility = if (result.isEmpty()) View.VISIBLE else View.GONE

        adapter.submitList(result)
    }

    // 7. Jangan lupa tambahkan onDestroyView
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
