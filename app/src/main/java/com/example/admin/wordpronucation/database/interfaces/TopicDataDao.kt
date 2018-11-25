package com.example.admin.wordpronucation.database.interfaces

import android.arch.persistence.room.*
import com.example.admin.wordpronucation.database.tables.TopicData

@Dao
interface TopicDataDao {
    @Query("SELECT * from TopicData")
    fun getAll(): List<TopicData>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(topicData: TopicData)

    @Query("DELETE from TopicData")
    fun deleteAll()

    @Update
    fun updateTopic(topicData: TopicData)

    @Query("SELECT * from TopicData WHERE id = (SELECT MAX(id) from TopicData)")
    fun getLastTopic(): TopicData

    @Query("SELECT * from TopicData WHERE id = :ID LIMIT 1")
    fun getTopicWithId(ID: Int): TopicData

    @Query("SELECT * from TopicData WHERE topic = :NAME LIMIT 1")
    fun getTopicWithName(NAME: String): TopicData
}