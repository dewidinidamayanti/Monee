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
import com.example.monee.databinding.FragmentAddTransactionBinding // 1. Impor kelas binding
import com.example.monee.db.Transaksi
import com.example.monee.db.TransaksiViewModel
import com.google.android.material.button.MaterialButton
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddTransactionFragment : Fragment() {

    // 2. Deklarasikan variabel binding
    private var _binding: FragmentAddTransactionBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: TransaksiViewModel
    private var selectedType = "Pengeluaran" // Default ke pengeluaran
    private var selectedTanggal: Long = System.currentTimeMillis()

    // 3. Gunakan onCreateView untuk inflate layout
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[TransaksiViewModel::class.java]

        // 4. Perbarui UI awal menggunakan binding
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.Builder().setLanguage("id").setRegion("ID").build())
        binding.etDate.setText(sdf.format(Date(selectedTanggal)))
        updateTypeUI(selectedType, binding.btnExpense, binding.btnIncome)

        // 5. Tambahkan listener dan logika lain menggunakan binding
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
            val cal = Calendar.getInstance()
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
            val title = binding.etTitle.text.toString().trim()
            val amountStr = binding.etAmount.text.toString()
            val category = binding.autoCompleteCategory.text.toString()

            if (title.isBlank() || amountStr.isBlank() || category.isBlank()) {
                Toast.makeText(requireContext(), "Lengkapi Judul, Nominal, dan Kategori", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val nominalValue = amountStr.replace(".", "").toDouble()

            val newTransaksi = Transaksi(
                judul = title,
                nominal = nominalValue,
                kategori = category,
                tipe = selectedType,
                tanggal = selectedTanggal,
                deskripsi = binding.etNote.text.toString().trim()
            )

            viewModel.insert(newTransaksi)
            findNavController().navigateUp()
        }


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

    // 6. Jangan lupa tambahkan onDestroyView
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
