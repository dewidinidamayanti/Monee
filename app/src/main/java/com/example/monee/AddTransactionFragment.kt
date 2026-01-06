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

class AddTransactionFragment : Fragment(R.layout.fragment_add_transaction) {

    private lateinit var viewModel: TransaksiViewModel
    private var selectedType = "Pengeluaran"
    private var selectedTanggal = System.currentTimeMillis()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[TransaksiViewModel::class.java]

        val etTitle: EditText = view.findViewById(R.id.etTitle)
        val etAmount: EditText = view.findViewById(R.id.etAmount)
        val etDate: EditText = view.findViewById(R.id.etDate)
        val autoCategory: AutoCompleteTextView = view.findViewById(R.id.autoCompleteCategory)
        val etNote: EditText = view.findViewById(R.id.etNote)

        val btnExpense: MaterialButton = view.findViewById(R.id.btnExpense)
        val btnIncome: MaterialButton = view.findViewById(R.id.btnIncome)
        val btnSave: MaterialButton = view.findViewById(R.id.btnSave)

        updateTypeUI("Pengeluaran", btnExpense, btnIncome)
        btnSave.text = "Tambah Pengeluaran"

        btnExpense.setOnClickListener {
            selectedType = "Pengeluaran"
            updateTypeUI(selectedType, btnExpense, btnIncome)
            btnSave.text = "Tambah Pengeluaran"
        }

        btnIncome.setOnClickListener {
            selectedType = "Pemasukan"
            updateTypeUI(selectedType, btnExpense, btnIncome)
            btnSave.text = "Tambah Pemasukan"
        }

        val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        etDate.setText(sdf.format(Date(selectedTanggal)))

        etDate.setOnClickListener {
            val cal = Calendar.getInstance()
            cal.timeInMillis = selectedTanggal

            DatePickerDialog(requireContext(), { _, y, m, d ->
                cal.set(y, m, d)
                selectedTanggal = cal.timeInMillis
                etDate.setText(sdf.format(Date(selectedTanggal)))
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        val categories = listOf(
            "Makanan", "Belanja", "Transportasi", "Utilitas",
            "Perumahan", "Hiburan", "Kesehatan", "Lainnya"
        )

        autoCategory.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        )

        btnSave.setOnClickListener {
            val transaksi = Transaksi(
                judul = etTitle.text.toString().trim(),
                kategori = autoCategory.text.toString(),
                nominal = etAmount.text.toString().toDoubleOrNull() ?: 0.0,
                tanggal = selectedTanggal,
                tipe = selectedType,
                deskripsi = etNote.text.toString().trim()
            )
            viewModel.insert(transaksi)
            findNavController().navigate(R.id.action_addTransactionFragment_to_homeFragment)
        }
    }

    private fun updateTypeUI(type: String, btnExpense: MaterialButton, btnIncome: MaterialButton) {
        if (type == "Pengeluaran") {
            btnExpense.setBackgroundColor(requireContext().getColor(R.color.expenseRed))
            btnExpense.setTextColor(requireContext().getColor(android.R.color.white))

            btnIncome.setBackgroundColor(requireContext().getColor(android.R.color.white))
            btnIncome.setTextColor(requireContext().getColor(R.color.textSecondary))
        } else {
            btnIncome.setBackgroundColor(requireContext().getColor(R.color.incomeGreen))
            btnIncome.setTextColor(requireContext().getColor(android.R.color.white))

            btnExpense.setBackgroundColor(requireContext().getColor(android.R.color.white))
            btnExpense.setTextColor(requireContext().getColor(R.color.textSecondary))
        }
    }
}
