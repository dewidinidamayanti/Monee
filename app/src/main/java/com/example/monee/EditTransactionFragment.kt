package com.example.monee

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.monee.databinding.FragmentEditTransactionBinding // Impor kelas binding
import com.example.monee.db.Transaksi
import com.example.monee.db.TransaksiViewModel
import com.google.android.material.button.MaterialButton
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Hapus R.layout.fragment_edit_transaction dari sini
class EditTransactionFragment : Fragment() {

    // Deklarasi variabel binding
    private var _binding: FragmentEditTransactionBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: TransaksiViewModel
    private var transaksiId = -1
    private lateinit var transaksiData: Transaksi

    private var selectedType = "Pengeluaran"
    private var selectedTanggal: Long = System.currentTimeMillis()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[TransaksiViewModel::class.java]

        // Hapus semua `view.findViewById`

        transaksiId = arguments?.getInt("transaksiId") ?: -1
        if (transaksiId == -1) {
            findNavController().navigateUp()
            return
        }

        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.Builder().setLanguage("id").setRegion("ID").build())
        val formatter = DecimalFormat("#,###")

        viewModel.getById(transaksiId).observe(viewLifecycleOwner) { data ->
            transaksiData = data

            // Gunakan `binding` untuk mengakses view
            binding.etTitle.setText(data.judul)
            binding.etAmount.setText(formatter.format(data.nominal))
            binding.autoCompleteCategory.setText(data.kategori, false)
            binding.etNote.setText(data.deskripsi)

            selectedTanggal = data.tanggal
            binding.etDate.setText(sdf.format(Date(data.tanggal)))

            selectedType = data.tipe
            updateTypeUI(selectedType, binding.btnExpense, binding.btnIncome)
        }

        binding.etAmount.addTextChangedListener(CurrencyTextWatcher(binding.etAmount))

        binding.btnExpense.setOnClickListener {
            selectedType = "Pengeluaran"
            updateTypeUI(selectedType, binding.btnExpense, binding.btnIncome)
        }

        binding.btnIncome.setOnClickListener {
            selectedType = "Pemasukan"
            updateTypeUI(selectedType, binding.btnExpense, binding.btnIncome)
        }

        binding.etDate.setOnClickListener {
            val cal = Calendar.getInstance().apply { timeInMillis = selectedTanggal }

            DatePickerDialog(requireContext(), { _, y, m, d ->
                cal.set(y, m, d)
                selectedTanggal = cal.timeInMillis
                binding.etDate.setText(sdf.format(Date(selectedTanggal)))
            },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.btnSave.setOnClickListener {
            if (binding.etTitle.text.isNullOrBlank() || binding.etAmount.text.isNullOrBlank()) {
                Toast.makeText(requireContext(), "Lengkapi data terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val nominalString = binding.etAmount.text.toString().replace(".", "")
            val nominalValue = if (nominalString.isNotEmpty()) nominalString.toDouble() else 0.0

            val updated = transaksiData.copy(
                judul = binding.etTitle.text.toString().trim(),
                nominal = nominalValue,
                kategori = binding.autoCompleteCategory.text.toString(),
                tipe = selectedType,
                tanggal = selectedTanggal,
                deskripsi = binding.etNote.text.toString().trim()
            )

            viewModel.update(updated)
            findNavController().navigate(R.id.action_editTransactionFragment_to_homeFragment)
        }

        binding.btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnClose.setOnClickListener { findNavController().navigateUp() }

        val categories = listOf(
            "Makanan", "Belanja", "Transportasi", "Utilitas",
            "Perumahan", "Hiburan", "Kesehatan", "Pendidikan", "Gaji", "Hadiah", "Lainnya"
        )

        binding.autoCompleteCategory.setAdapter(
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Kelas CurrencyTextWatcher tidak perlu diubah dan bisa tetap di sini
class CurrencyTextWatcher(private val editText: EditText) : TextWatcher {
    private val decimalFormat = DecimalFormat("#,###")
    init {
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
            originalString = originalString.replace("[^\\d]".toRegex(), "")
            if (originalString.isNotEmpty()) {
                val longval = originalString.toLong()
                val formattedString = decimalFormat.format(longval)
                editText.setText(formattedString)
                editText.setSelection(editText.text.length)
            } else {
                editText.setText("")
            }
        } catch (nfe: NumberFormatException) {
            nfe.printStackTrace()
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        editText.addTextChangedListener(this)
    }
}
