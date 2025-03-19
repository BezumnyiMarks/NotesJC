package com.example.notesjc.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.notesjc.data.Image
import com.example.notesjc.data.Note
import dagger.Module
import dagger.hilt.InstallIn


@Database(entities = [Note::class, Image::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract  fun notesDao(): NotesDao
}