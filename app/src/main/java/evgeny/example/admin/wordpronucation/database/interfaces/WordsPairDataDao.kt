package evgeny.example.admin.wordpronucation.database.interfaces

import android.arch.persistence.room.*
import evgeny.example.admin.wordpronucation.database.tables.WordsPairData

@Dao
interface WordsPairDataDao {
    @Query("SELECT * from WordsPairData")
    fun getAll(): List<WordsPairData>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(wordsPairData: WordsPairData)

    @Query("DELETE from WordsPairData")
    fun deleteAll()

    @Query("SELECT * from WordsPairData WHERE id = (SELECT MAX(id) from WordsPairData)")
    fun getLastWordsPair(): WordsPairData

    @Query("SELECT * from WordsPairData WHERE id = :ID LIMIT 1 ")
    fun getWordsPairWithId(ID: Int): WordsPairData

    @Update
    fun updateWordsPair(wordsPairData: WordsPairData)

}