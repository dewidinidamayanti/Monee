package com.example.monee.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TransaksiDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaksi: Transaksi)

    @Update
    suspend fun update(transaksi: Transaksi)

    @Delete
    suspend fun delete(transaksi: Transaksi)

    @Query("SELECT * FROM transaksi ORDER BY tanggal DESC")
    fun getAll(): LiveData<List<Transaksi>>

    @Query("SELECT * FROM transaksi WHERE id = :id")
    fun getById(id: Int): LiveData<Transaksi>

    @Query("SELECT SUM(nominal) FROM transaksi WHERE tipe = 'pemasukan'")
    fun getTotalPemasukan(): LiveData<Double?>

    @Query("SELECT SUM(nominal) FROM transaksi WHERE tipe = 'pengeluaran'")
    fun getTotalPengeluaran(): LiveData<Double?>
}
