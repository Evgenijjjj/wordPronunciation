package evgeny.example.admin.wordpronucation.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import evgeny.example.admin.wordpronucation.database.interfaces.TopicDataDao
import evgeny.example.admin.wordpronucation.database.interfaces.WordsPairDataDao
import evgeny.example.admin.wordpronucation.database.tables.TopicData
import evgeny.example.admin.wordpronucation.database.tables.WordsPairData

@Database(entities = arrayOf(TopicData::class, WordsPairData::class), version = 1)
abstract class VocabularyDataBase: RoomDatabase() {

    abstract fun TopicDataDao(): TopicDataDao
    abstract fun WordsPairDataDao(): WordsPairDataDao

    companion object {
        private var instance: VocabularyDataBase? = null

        fun getInstance(ctx: Context): VocabularyDataBase? {
            if (instance == null) {
                synchronized(VocabularyDataBase::class) {
                    instance = Room.databaseBuilder(ctx.applicationContext, VocabularyDataBase::class.java, "vocabulary.0.1")
                        .allowMainThreadQueries()
                        .build()
                }
            }
            return instance
        }

        fun destroyInstance() {
            instance = null
        }
    }

}