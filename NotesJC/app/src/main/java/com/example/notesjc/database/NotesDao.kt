package com.example.notesjc.database

import androidx.room.*
import com.example.notesjc.data.FullNote
import com.example.notesjc.data.Image
import com.example.notesjc.data.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@Dao
interface NotesDao {

    @Transaction
    @Query("SELECT * FROM Note WHERE relevant = 1")
    suspend fun getAll(): List<FullNote>

    @Query("SELECT * FROM Note WHERE noteDateTimeID = :noteDateTimeID AND relevant = 1")
    suspend fun getByDateTime(noteDateTimeID: Long): FullNote

    @Query("SELECT * FROM Note WHERE category = :category AND relevant = 1")
    suspend fun getByCategory(category: String): List<FullNote>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: Image)

    @Delete
    suspend fun deleteNotes(note: List<Note>)

    @Delete
    suspend fun deleteImages(image: List<Image>)

}