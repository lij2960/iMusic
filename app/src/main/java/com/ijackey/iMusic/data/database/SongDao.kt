package com.ijackey.iMusic.data.database

import androidx.room.*
import com.ijackey.iMusic.data.model.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs ORDER BY dateAdded DESC")
    fun getAllSongs(): Flow<List<Song>>
    
    @Query("SELECT * FROM songs ORDER BY dateAdded DESC")
    suspend fun getAllSongsSync(): List<Song>
    
    @Query("SELECT * FROM songs WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%'")
    fun searchSongs(query: String): Flow<List<Song>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<Song>)
    
    @Delete
    suspend fun deleteSong(song: Song)
    
    @Update
    suspend fun updateSong(song: Song)
    
    @Query("DELETE FROM songs")
    suspend fun deleteAllSongs()
}