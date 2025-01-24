package com.example.bai1

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface NoteDao {
    @Insert
    suspend fun insert(note: Note)

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(vararg notes: Note)

    @Query("SELECT * FROM notes")
    suspend fun getAll(): MutableList<Note>
}