package com.example.admin.wordpronucation.database.tables

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "WordsPairData")
data class WordsPairData(
    @PrimaryKey(autoGenerate = true) var id: Int?,
    @ColumnInfo(name = "engWord") var engWord: String,
    @ColumnInfo(name = "translatedWord") var translatedWord: String,
    @ColumnInfo(name = "nextWordsPairDataId") var nextWordsPairDataId: Int?
) {
    constructor(): this(null, "", "", null)
}