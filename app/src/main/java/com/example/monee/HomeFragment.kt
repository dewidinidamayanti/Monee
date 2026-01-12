package com.example.monee

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.monee.databinding.FragmentHomeBinding
import com.example.monee.db.Transaksi
import com.example.monee.db.TransaksiViewModel
import java.text.DecimalFormat

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: TransaksiViewModel
    private lateinit var transaksiAdapter: TransaksiAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[TransaksiViewModel::class.java]

        transaksiAdapter = TransaksiAdapter(
            onEditClick = { transaksi ->
                val bundle = Bundle().apply { putInt("transaksiId", transaksi.id) }
                findNavController().navigate(R.id.action_homeFragment_to_editTransactionFragment, bundle)
            },
            onDeleteClick = { transaksi -> confirmDelete(transaksi) }
        )

        binding.rvTransaksiTerkini.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTransaksiTerkini.adapter = transaksiAdapter

        binding.tvLihatSemua.setOnClickListener {
            findNavController().navigate(
                R.id.action_homeFragment_to_allTransactionFragment,
                null,
                NavOptions.Builder()
                    .setRestoreState(true)
                    .setLaunchSingleTop(true)
                    .build()
            )
        }

        // Tampilkan ProgressBar saat mulai memuat data
        binding.pbHome.visibility = View.VISIBLE
        binding.rvTransaksiTerkini.visibility = View.GONE

        viewModel.allTransaksi.observe(viewLifecycleOwner) { list ->
            try {
                // Sembunyikan ProgressBar setelah data diterima
                binding.pbHome.visibility = View.GONE

                if (list.isNullOrEmpty()) {
                    binding.tvTotalSaldo.text = getString(R.string.rupiah_zero)
                    binding.tvTotalPemasukan.text = getString(R.string.rupiah_zero)
                    binding.tvTotalPengeluaran.text = getString(R.string.rupiah_zero)
                    binding.tvMiniIncome.text = getString(R.string.rupiah_zero)
                    binding.tvMiniExpense.text = getString(R.string.rupiah_zero)

                    transaksiAdapter.submitList(emptyList())
                    binding.tvHomeEmpty.visibility = View.VISIBLE
                    binding.rvTransaksiTerkini.visibility = View.GONE
                    return@observe
                }

                binding.tvHomeEmpty.visibility = View.GONE
                binding.rvTransaksiTerkini.visibility = View.VISIBLE

                val totalIncome = list.filter { it.tipe.equals("Pemasukan", ignoreCase = true) }.sumOf { it.nominal }
                val totalExpense = list.filter { it.tipe.equals("Pengeluaran", ignoreCase = true) }.sumOf { it.nominal }
                val saldo = totalIncome - totalExpense

                binding.tvTotalSaldo.text = formatRupiah(saldo)
                binding.tvTotalPemasukan.text = formatRupiah(totalIncome)
                binding.tvTotalPengeluaran.text = formatRupiah(totalExpense)
                binding.tvMiniIncome.text = formatRupiah(totalIncome)
                binding.tvMiniExpense.text = formatRupiah(totalExpense)

                transaksiAdapter.submitList(list.sortedByDescending { it.tanggal }.take(10))

            } catch (e: Exception) {
                binding.pbHome.visibility = View.GONE
                binding.rvTransaksiTerkini.visibility = View.GONE
                binding.tvHomeEmpty.visibility = View.VISIBLE
                binding.tvHomeEmpty.text = getString(R.string.gagal_memuat)
                Log.e("HomeFragment", "Error processing data", e)
            }
        }
    }

    private fun confirmDelete(transaksi: Transaksi) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.hapus_transaksi_title)
            .setMessage(R.string.hapus_transaksi_msg)
            .setPositiveButton(R.string.hapus) { _, _ -> viewModel.delete(transaksi) }
            .setNegativeButton(R.string.batal, null)
            .show()
    }

    private fun formatRupiah(amount: Double): String {
        val formatter = DecimalFormat("#,###")
        val symbols = formatter.decimalFormatSymbols
        symbols.groupingSeparator = '.'
        formatter.decimalFormatSymbols = symbols
        return "Rp${formatter.format(amount)}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
