package com.example.notesjc.viewmodels

import android.app.Application
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract.CommonDataKinds.Im
import android.util.Log
import androidx.compose.ui.res.colorResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesjc.R
import com.example.notesjc.data.FullNote
import com.example.notesjc.data.Image
import com.example.notesjc.data.Note
import com.example.notesjc.database.AppDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class DBViewModel @Inject constructor(
    private val appDatabase: AppDatabase,
    @ApplicationContext
    private val context: Context
): ViewModel() {
    val allNotes = MutableStateFlow<List<FullNote>>(listOf())
    val mediaStoreImagesUri = MutableStateFlow<List<Uri>>(listOf())
    val chosenImagesUri = MutableStateFlow<List<Uri>>(listOf())
    val currentNote = MutableStateFlow(FullNote())
    val onNewIntentGetExtra = MutableStateFlow(0L)

    fun clearIntentExtra(){
        onNewIntentGetExtra.value = 0L
    }

    fun saveNote(relevant: Boolean){
        viewModelScope.launch {
            val dateTimeMillis: Long = if (currentNote.value.note?.noteDateTimeID == null)
                Calendar.getInstance().timeInMillis
            else currentNote.value.note?.noteDateTimeID ?: 0L

            val category = if (currentNote.value.note?.category.isNullOrEmpty())
                context.getString(R.string.no_category)
            else currentNote.value.note?.category

            currentNote.value.note = currentNote.value.note?.copy(
                noteDateTimeID = dateTimeMillis,
                category = category,
                relevant = relevant
            )
            appDatabase.notesDao().insertNote(currentNote.value.note ?: Note())

            saveImages(dateTimeMillis)
        }
    }

    private fun saveImages(dateTimeMillis: Long){
        viewModelScope.launch {
            val oldDbImages = appDatabase.notesDao().getByDateTime(dateTimeMillis).images
            currentNote.value.images?.forEach { image ->
                if (oldDbImages?.count{
                        it.imageURI == image.imageURI
                    } == 0)
                    appDatabase.notesDao().insertImage(image.copy(noteDateTimeID = dateTimeMillis))
            }
            oldDbImages?.forEach { oldImage ->
                if (currentNote.value.images?.count{
                        oldImage.imageURI == it.imageURI
                    } == 0)
                    appDatabase.notesDao().deleteImages(listOf(oldImage))
            }
            currentNote.value = FullNote()
        }
    }

    fun getByDateTime(dateTime: Long){
        viewModelScope.launch {
            val note = appDatabase.notesDao().getByDateTime(dateTime)
            currentNote.value = note
        }
    }

    fun getByCategory(category: String){
        viewModelScope.launch {
            allNotes.value = appDatabase.notesDao().getByCategory(category)
        }
    }

    fun getAll(){
        viewModelScope.launch {
            allNotes.value = appDatabase.notesDao().getAll()
        }
    }

    fun delete(fullNotesList: List<FullNote>){
        viewModelScope.launch {
            val notes = mutableListOf<Note>()
            val images = mutableListOf<Image>()
            fullNotesList.forEach { fullNote ->
                notes.add(fullNote.note ?: Note())
                fullNote.images?.forEach { image ->
                    images.add(image)
                }
            }
            appDatabase.notesDao().deleteNotes(notes)
            appDatabase.notesDao().deleteImages(images)
        }
    }

    fun updateCurrentCategory(category: String){
        currentNote.value.note = currentNote.value.note?.copy(category = category)
    }

    fun updateCurrentDescription(description: String){
        currentNote.value.note = currentNote.value.note?.copy(description = description)
    }

    fun updateCurrentAlarmDateTime(dateTimeMillis: Long?){
        val note = currentNote.value
        currentNote.value = FullNote(note.note?.copy(alarmDateTime = dateTimeMillis), note.images)
    }

    fun updateCurrentPriority(priority: Int){
        currentNote.value.note = currentNote.value.note?.copy(priority = priority)
    }

    fun addCurrentImages(imagesUriList: List<Uri>){
        val images = currentNote.value.images?.toMutableList()
        imagesUriList.forEach { uri ->
            if (images?.count {
                    it.imageURI == uri.toString()
                } == 0
            ) {
                images.add(
                    Image(
                        imageURI = uri.toString(),
                        noteDateTimeID = 0L
                    )
                )
            }
        }
        currentNote.value.images = images
    }

    fun removeCurrentImage(image: Image){
        val note = currentNote.value
        val images = note.images?.toMutableList()
        images?.remove(image)
        currentNote.value = FullNote(note.note, images)
    }

    fun clearCurrentNote(){
        currentNote.value = FullNote()
    }

    fun addChosenImage(imageUri: Uri){
        val chosenImages = chosenImagesUri.value.toMutableList()
        chosenImages.add(imageUri)
        chosenImagesUri.value = chosenImages
    }

    fun removeChosenImage(imageUri: Uri){
        val chosenImages = chosenImagesUri.value.toMutableList()
        chosenImages.remove(imageUri)
        chosenImagesUri.value = chosenImages
    }

    fun clearChosenImagesList(){
        chosenImagesUri.value = listOf()
    }
}