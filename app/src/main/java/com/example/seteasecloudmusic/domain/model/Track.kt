package com.example.seteasecloudmusic.domain.model

data class Track(
    val id: Long,
    val title: String,
    val artists: List<Artist> = emptyList(),
    val album: Album,

    val qualityTags: List<AudioQuality> = emptyList(),

    val coverUrl: String? = null,
    val durationMs: Long? = null,

    val playableUrl: String? = null,
    val isPlayable: Boolean = true
)

data class Album(
    val id: Long,
    val title: String,
    val coverUrl: String? = null
)

data class Artist(
    val id: Long,
    val name: String,
    val coverUrl: String? = null
)

enum class AudioQuality{
    STANDARD,
    HIGH,
    LOSSLESS,
    HIRES
}