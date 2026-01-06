package com.example.monee

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.monee.db.Transaksi
import com.example.monee.db.TransaksiViewModel
import com.google.android.material.button.MaterialButton
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class EditTransactionFragment : Fragment(R.layout.fragment_edit_transaction) {

    private lateinit var viewModel: TransaksiViewModel
    private var transaksiId = -1
    private lateinit var transaksiData: Transaksi

    private var selectedType = "Pengeluaran"
    private var selectedTanggal: Long = System.currentTimeMillis()

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
        val btnCancel: MaterialButton = view.findViewById(R.id.btnCancel)
        val btnClose: ImageView = view.findViewById(R.id.btnClose)

        transaksiId = arguments?.getInt("transaksiId") ?: -1
        if (transaksiId == -1) {
            findNavController().navigateUp()
            return
        }

        val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        val formatter = DecimalFormat("#,###")

        viewModel.getById(transaksiId).observe(viewLifecycleOwner) { data ->
            transaksiData = data

            etTitle.setText(data.judul)
            etAmount.setText(formatter.format(data.nominal))
            autoCategory.setText(data.kategori, false)
            etNote.setText(data.deskripsi)

            selectedTanggal = data.tanggal
            etDate.setText(sdf.format(Date(data.tanggal)))

            selectedType = data.tipe
            updateTypeUI(selectedType, btnExpense, btnIncome)
        }

        // Menggunakan CurrencyTextWatcher yang sudah didefinisikan di bawah
        etAmount.addTextChangedListener(CurrencyTextWatcher(etAmount))

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

            val nominalString = etAmount.text.toString().replace(".", "")

            val nominalValue = if (nominalString.isNotEmpty()) nominalString.toDouble() else 0.0

            val updated = transaksiData.copy(
                judul = etTitle.text.toString().trim(),
                nominal = nominalValue,
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
        if (type.equals("Pengeluaran", ignoreCase = true)) {
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

class CurrencyTextWatcher(private val editText: EditText) : TextWatcher {

    private val decimalFormat = DecimalFormat("#,###")
    init {
        // Ini untuk memastikan formatnya menggunakan titik sebagai pemisah ribuan
        // sesuai dengan Locale Indonesia.
        decimalFormat.isGroupingUsed = true
        decimalFormat.maximumFractionDigits = 0
        val symbols = decimalFormat.decimalFormatSymbols
        symbols.groupingSeparator = '.'
        decimalFormat.decimalFormatSymbols = symbols
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        editText.removeTextChangedListener(this)

        try {
            var originalString = s.toString()

            // Hapus semua karakter non-digit (koma, titik, dll)
            originalString = originalString.replace("[^\\d]".toRegex(), "")

            if (originalString.isNotEmpty()) {
                val longval = originalString.toLong()
                val formattedString = decimalFormat.format(longval)

                // Setel teks yang sudah diformat ke EditText
                editText.setText(formattedString)
                editText.setSelection(editText.text.length)
            } else {
                editText.setText("")
            }
        } catch (nfe: NumberFormatException) {
            // Tangani jika terjadi kesalahan parsing (misal, angka terlalu besar)
            nfe.printStackTrace()
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        editText.addTextChangedListener(this)
    }
}
