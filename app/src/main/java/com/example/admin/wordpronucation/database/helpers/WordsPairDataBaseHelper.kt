package com.example.admin.wordpronucation.database.helpers

import android.content.Context
import com.example.admin.wordpronucation.database.VocabularyDataBase
import com.example.admin.wordpronucation.database.interfaces.WordsPairDataDao
import com.example.admin.wordpronucation.database.tables.TopicData
import com.example.admin.wordpronucation.database.tables.WordsPairData
import com.example.admin.wordpronucation.models.WordPair

class WordsPairDataBaseHelper {
    private var ctx: Context

    constructor(context: Context) {
        this.ctx = context
    }

    fun addWordsPairInDB(wp: WordsPairData, topicData: TopicData) {
        val wordsPairDataDao = VocabularyDataBase.getInstance(ctx)?.WordsPairDataDao()
        val topicDataDao = VocabularyDataBase.getInstance(ctx)?.TopicDataDao()

        wordsPairDataDao?.insert(wp)
        val wordsPairData = wordsPairDataDao?.getLastWordsPair()

        if (topicData.wordsPairDataId != null) {
            var wordsPairDataPtr = wordsPairDataDao?.getWordsPairWithId(topicData.wordsPairDataId!!)

            while (wordsPairDataPtr?.nextWordsPairDataId != null)
                wordsPairDataPtr = wordsPairDataDao?.getWordsPairWithId(wordsPairDataPtr.nextWordsPairDataId!!)

            wordsPairDataPtr?.nextWordsPairDataId = wordsPairData?.id
            wordsPairDataDao?.updateWordsPair(wordsPairDataPtr!!)
        }
        else {
            topicData.wordsPairDataId = wordsPairData?.id
            topicDataDao?.updateTopic(topicData)
        }
    }

    fun getAllWordPairsForTopic(topicData: TopicData): ArrayList<WordPair>? {
        if (topicData.wordsPairDataId == null) return null

        val wordsPairDataDao = VocabularyDataBase.getInstance(ctx)?.WordsPairDataDao()
        val list: ArrayList<WordPair> = arrayListOf()
        var wordsPairDataPtr = wordsPairDataDao?.getWordsPairWithId(topicData.wordsPairDataId!!)
        list.add(WordPair(wordsPairDataPtr?.engWord!!,wordsPairDataPtr.translatedWord))

        while (wordsPairDataPtr?.nextWordsPairDataId != null) {
            wordsPairDataPtr = wordsPairDataDao?.getWordsPairWithId(wordsPairDataPtr.nextWordsPairDataId!!)
            list.add(WordPair(wordsPairDataPtr?.engWord!!,wordsPairDataPtr?.translatedWord!!))
        }

        return list
    }
}