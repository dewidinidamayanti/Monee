package com.example.monee.db

import androidx.lifecycle.LiveData

class TransaksiRepository(private val dao: TransaksiDao) {

    fun getAll(): LiveData<List<Transaksi>> = dao.getAll()

    fun getById(id: Int): LiveData<Transaksi> = dao.getById(id)

    fun getTotalPemasukan(): LiveData<Double?> = dao.getTotalPemasukan()

    fun getTotalPengeluaran(): LiveData<Double?> = dao.getTotalPengeluaran()

    suspend fun insert(transaksi: Transaksi) {
        dao.insert(transaksi)
    }

    suspend fun update(transaksi: Transaksi) {
        dao.update(transaksi)
    }

    suspend fun delete(transaksi: Transaksi) {
        dao.delete(transaksi)
    }
}
