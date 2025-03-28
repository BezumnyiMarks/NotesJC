package com.example.notesjc.data

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer

data class FullNote(
    @Embedded
    var note: Note = Note(),

    @Relation(entity = Image::class, parentColumn = "noteDateTimeID", entityColumn = "noteDateTimeID")
    var images: List<Image> ?= listOf(),
)

@Entity(tableName = "Note")
data class Note(
    @PrimaryKey
    @ColumnInfo(name = "noteDateTimeID")
    val noteDateTimeID: Long = 0,
    @ColumnInfo(name = "category")
    val category: String = "",
    @ColumnInfo(name = "description")
    val description: String = "",
    @ColumnInfo(name = "alarmDateTime")
    val alarmDateTime: Long = 0L,
    @ColumnInfo(name = "priority")
    val priority: Int = 3,
    @ColumnInfo(name = "relevant")
    val relevant: Boolean = true
): java.io.Serializable


@Entity(tableName = "Image")
data class Image(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long ?= null,

    @ColumnInfo(name = "imageURI")
    val imageURI: String ?= null,

    @ColumnInfo(name = "noteDateTimeID")
    var noteDateTimeID: Long ?= null,
)
