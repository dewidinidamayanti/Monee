package com.example.monee.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transaksi")

data class Transaksi(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val judul: String,
    val kategori: String,
    val nominal: Double,
    val tanggal: Long,
    val tipe: String,
    val deskripsi: String = ""
)
