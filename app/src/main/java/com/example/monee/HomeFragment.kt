package com.example.monee

import android.os.Bundle
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
        // Inflate layout menggunakan binding
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root // Kembalikan root view dari binding
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
                null, // Tidak ada bundle
                NavOptions.Builder()
                    .setRestoreState(true) // Simpan state saat kembali
                    .setLaunchSingleTop(true) // Hindari menumpuk fragmen yang sama
                    .build()
            )
        }

        // Tampilkan ProgressBar sebelum mengamati data
        binding.progressBar.visibility = View.VISIBLE
        binding.rvTransaksiTerkini.visibility = View.GONE // Sembunyikan daftar saat loading

        viewModel.allTransaksi.observe(viewLifecycleOwner) { list ->
            try {
                // Selalu sembunyikan ProgressBar saat data diterima (sukses atau gagal di awal)
                binding.progressBar.visibility = View.GONE

                if (list.isNullOrEmpty()) {
                    // Blok ini dieksekusi jika daftar transaksi kosong
                    binding.tvTotalSaldo.text = "Rp0"
                    binding.tvTotalPemasukan.text = "Rp0"
                    binding.tvTotalPengeluaran.text = "Rp0"
                    binding.tvMiniIncome.text = "Rp0"
                    binding.tvMiniExpense.text = "Rp0"

                    transaksiAdapter.submitList(emptyList()) // Kosongkan adapter
                    binding.tvHomeEmpty.visibility = View.VISIBLE // Tampilkan pesan "kosong"
                    binding.rvTransaksiTerkini.visibility = View.GONE // Sembunyikan daftar
                    return@observe // Keluar dari observe karena tidak ada lagi yang perlu diproses
                }

                // Jika daftar tidak kosong, lanjutkan di sini
                binding.tvHomeEmpty.visibility = View.GONE // Sembunyikan pesan "kosong"
                binding.rvTransaksiTerkini.visibility = View.VISIBLE // Tampilkan daftar

                // Lakukan kalkulasi
                val totalIncome = list.filter { it.tipe.equals("Pemasukan", ignoreCase = true) }.sumOf { it.nominal }
                val totalExpense = list.filter { it.tipe.equals("Pengeluaran", ignoreCase = true) }.sumOf { it.nominal }
                val saldo = totalIncome - totalExpense

                // Update UI dengan data yang sudah diformat
                binding.tvTotalSaldo.text = formatRupiah(saldo)
                binding.tvTotalPemasukan.text = formatRupiah(totalIncome)
                binding.tvTotalPengeluaran.text = formatRupiah(totalExpense)
                binding.tvMiniIncome.text = formatRupiah(totalIncome)
                binding.tvMiniExpense.text = formatRupiah(totalExpense)

                // Tampilkan 10 transaksi terbaru ke RecyclerView
                transaksiAdapter.submitList(list.sortedByDescending { it.tanggal }.take(10))

            } catch (e: Exception) {
                // Blok ini hanya berjalan jika terjadi error tak terduga (misal saat formatRupiah)
                binding.progressBar.visibility = View.GONE // Pastikan ProgressBar tetap hilang
                binding.rvTransaksiTerkini.visibility = View.GONE // Sembunyikan daftar
                binding.tvHomeEmpty.visibility = View.VISIBLE // Tampilkan pesan error
                binding.tvHomeEmpty.text = "Gagal memuat data" // Beri tahu pengguna ada masalah

                android.util.Log.e("HomeFragment", "Gagal memproses daftar transaksi", e)
            }
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
        val formatter = DecimalFormat("#,###")
        val symbols = formatter.decimalFormatSymbols
        symbols.groupingSeparator = '.'
        formatter.decimalFormatSymbols = symbols
        formatter.isGroupingUsed = true
        formatter.maximumFractionDigits = 0
        return "Rp${formatter.format(amount)}"
    }

    // Penting: Bersihkan referensi binding untuk menghindari memory leak
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
