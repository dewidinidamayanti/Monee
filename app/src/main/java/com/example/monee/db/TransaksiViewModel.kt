package com.example.monee.db

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TransaksiViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TransaksiRepository

    val allTransaksi: LiveData<List<Transaksi>>
    val totalPemasukan: LiveData<Double>
    val totalPengeluaran: LiveData<Double>

    init {
        val dao = AppDatabase.getDatabase(application).transaksiDao()
        repository = TransaksiRepository(dao)

        allTransaksi = repository.getAll()
        totalPemasukan = repository.getTotalPemasukan().map { it ?: 0.0 }
        totalPengeluaran = repository.getTotalPengeluaran().map { it ?: 0.0 }
    }

    fun insert(transaksi: Transaksi) =
        viewModelScope.launch(Dispatchers.IO) { repository.insert(transaksi) }

    fun update(transaksi: Transaksi) =
        viewModelScope.launch(Dispatchers.IO) { repository.update(transaksi) }

    fun delete(transaksi: Transaksi) =
        viewModelScope.launch(Dispatchers.IO) { repository.delete(transaksi) }

    fun getById(id: Int): LiveData<Transaksi> =
        repository.getById(id)
}
