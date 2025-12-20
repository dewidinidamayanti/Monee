package com.example.monee

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.monee.db.Transaksi
import com.example.monee.db.TransaksiViewModel
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

class EditTransactionFragment : Fragment(R.layout.fragment_edit_transaction) {

    private lateinit var viewModel: TransaksiViewModel
    private var transaksiId: Int = -1
    private lateinit var transaksiData: Transaksi
    private var selectedType: String = "pengeluaran"
    private var selectedTanggal: Long = 0L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[TransaksiViewModel::class.java]

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
        if (transaksiId == -1) return

        fun selectType(type: String) {
            selectedType = type
            if (type == "pengeluaran") {
                btnExpense.setBackgroundColor(resources.getColor(R.color.expenseRed, null))
                btnExpense.setTextColor(resources.getColor(android.R.color.white, null))
                btnIncome.setBackgroundColor(resources.getColor(R.color.white, null))
                btnIncome.setTextColor(resources.getColor(R.color.textSecondary, null))
            } else {
                btnIncome.setBackgroundColor(resources.getColor(R.color.incomeGreen, null))
                btnIncome.setTextColor(resources.getColor(android.R.color.white, null))
                btnExpense.setBackgroundColor(resources.getColor(R.color.white, null))
                btnExpense.setTextColor(resources.getColor(R.color.textSecondary, null))
            }
        }

        btnExpense.setOnClickListener { selectType("pengeluaran") }
        btnIncome.setOnClickListener { selectType("pemasukan") }

        viewModel.getById(transaksiId).observe(viewLifecycleOwner) { transaksi ->
            transaksi?.let {
                transaksiData = it

                etTitle.setText(it.judul)
                etAmount.setText(it.nominal.toString())
                autoCategory.setText(it.kategori, false)
                etNote.setText(it.deskripsi ?: "")

                selectedTanggal = it.tanggal
                val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
                etDate.setText(sdf.format(Date(it.tanggal)))

                selectType(it.tipe)
            }
        }

        etDate.setOnClickListener {
            val cal = Calendar.getInstance()
            cal.timeInMillis = selectedTanggal.takeIf { it != 0L } ?: System.currentTimeMillis()

            DatePickerDialog(
                requireContext(),
                { _, y, m, d ->
                    cal.set(y, m, d)
                    selectedTanggal = cal.timeInMillis
                    val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
                    etDate.setText(sdf.format(Date(selectedTanggal)))
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        btnSave.setOnClickListener {
            val updated = transaksiData.copy(
                judul = etTitle.text.toString().trim(),
                nominal = etAmount.text.toString().toDoubleOrNull() ?: 0.0,
                kategori = autoCategory.text.toString(),
                tipe = selectedType,
                tanggal = selectedTanggal,
                deskripsi = etNote.text.toString().trim()
            )
            viewModel.update(updated)
            requireActivity().onBackPressed()
        }

        btnCancel.setOnClickListener {
            viewModel.delete(transaksiData)
            requireActivity().onBackPressed()
        }

        btnClose.setOnClickListener {
            requireActivity().onBackPressed()
        }

        val categories = listOf(
            "Makanan", "Belanja", "Transportasi", "Utilitas",
            "Perumahan", "Hiburan", "Kesehatan", "Lainnya"
        )
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        autoCategory.setAdapter(adapter)
    }
}
