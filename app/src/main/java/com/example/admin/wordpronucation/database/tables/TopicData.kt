package com.example.admin.wordpronucation.database.tables

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "TopicData")
data class TopicData(
    @PrimaryKey(autoGenerate = true) var id: Int?,
    @ColumnInfo(name = "topic") var topic: String,
    @ColumnInfo(name = "wordsPairDataId") var wordsPairDataId: Int?
) {
    constructor(): this(null, "", null)
}