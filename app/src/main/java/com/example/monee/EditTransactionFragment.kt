package com.example.monee

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.monee.db.Transaksi
import com.example.monee.db.TransaksiViewModel
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

class EditTransactionFragment : Fragment(R.layout.fragment_edit_transaction) {

    private lateinit var viewModel: TransaksiViewModel
    private var transaksiId = -1
    private lateinit var transaksiData: Transaksi

    private var selectedType = "Pengeluaran"
    private var selectedTanggal: Long = System.currentTimeMillis()   // ⭐ prevent zero date bug

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[TransaksiViewModel::class.java] // ⭐ samakan scope

        val etTitle: EditText = view.findViewById(R.id.etTitle)
        val etAmount: EditText = view.findViewById(R.id.etAmount)
        val etDate: EditText = view.findViewById(R.id.etDate)
        val autoCategory: AutoCompleteTextView = view.findViewById(R.id.autoCompleteCategory)
        val etNote: EditText = view.findViewById(R.id.etNote)

        val btnExpense: MaterialButton = view.findViewById(R.id.btnExpense)
        val btnIncome: MaterialButton = view.findViewById(R.id.btnIncome)
        val btnSave: MaterialButton = view.findViewById(R.id.btnSave)
        val btnCancel: MaterialButton = view.findViewById(R.id.btnCancel)
        val btnClose: ImageView = view.findViewById(R.id.btnClose)

        transaksiId = arguments?.getInt("transaksiId") ?: -1
        if (transaksiId == -1) {
            findNavController().navigateUp()
            return
        }

        val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))

        viewModel.getById(transaksiId).observe(viewLifecycleOwner) { data ->
            transaksiData = data

            etTitle.setText(data.judul)
            etAmount.setText(data.nominal.toString())
            autoCategory.setText(data.kategori, false)
            etNote.setText(data.deskripsi)

            selectedTanggal = data.tanggal
            etDate.setText(sdf.format(Date(data.tanggal)))

            selectedType = data.tipe
            updateTypeUI(selectedType, btnExpense, btnIncome)
        }

        btnExpense.setOnClickListener {
            selectedType = "Pengeluaran"
            updateTypeUI(selectedType, btnExpense, btnIncome)
        }

        btnIncome.setOnClickListener {
            selectedType = "Pemasukan"
            updateTypeUI(selectedType, btnExpense, btnIncome)
        }

        etDate.setOnClickListener {
            val cal = Calendar.getInstance().apply { timeInMillis = selectedTanggal }

            DatePickerDialog(requireContext(), { _, y, m, d ->
                cal.set(y, m, d)
                selectedTanggal = cal.timeInMillis
                etDate.setText(sdf.format(Date(selectedTanggal)))
            },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        btnSave.setOnClickListener {
            if (etTitle.text.isNullOrBlank() || etAmount.text.isNullOrBlank()) {
                Toast.makeText(requireContext(), "Lengkapi data terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updated = transaksiData.copy(
                judul = etTitle.text.toString().trim(),
                nominal = etAmount.text.toString().toDouble(),
                kategori = autoCategory.text.toString(),
                tipe = selectedType,
                tanggal = selectedTanggal,
                deskripsi = etNote.text.toString().trim()
            )

            viewModel.update(updated)
            findNavController().navigate(R.id.action_editTransactionFragment_to_homeFragment)
        }

        btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }

        btnClose.setOnClickListener { findNavController().navigateUp() }

        val categories = listOf(
            "Makanan", "Belanja", "Transportasi", "Utilitas",
            "Perumahan", "Hiburan", "Kesehatan", "Lainnya"
        )

        autoCategory.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        )
    }

    private fun updateTypeUI(type: String, btnExpense: MaterialButton, btnIncome: MaterialButton) {
        if (type == "Pengeluaran") {
            btnExpense.setBackgroundColor(requireContext().getColor(R.color.expenseRed))
            btnExpense.setTextColor(requireContext().getColor(android.R.color.white))

            btnIncome.setBackgroundColor(requireContext().getColor(android.R.color.white))
            btnIncome.setTextColor(requireContext().getColor(R.color.textSecondary))
        } else {
            btnIncome.setBackgroundColor(requireContext().getColor(R.color.primaryBlue))
            btnIncome.setTextColor(requireContext().getColor(android.R.color.white))

            btnExpense.setBackgroundColor(requireContext().getColor(android.R.color.white))
            btnExpense.setTextColor(requireContext().getColor(R.color.textSecondary))
        }
    }
}
