package com.ijackey.iMusic.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class Song(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val path: String,
    val dateAdded: Long,
    val size: Long,
    val albumArt: String? = null,
    val albumArtPath: String? = null
)

enum class PlayMode {
    SEQUENTIAL, SHUFFLE, REPEAT_ONE
}

enum class SortOrder {
    DATE_ADDED, TITLE, ARTIST, DURATION
}