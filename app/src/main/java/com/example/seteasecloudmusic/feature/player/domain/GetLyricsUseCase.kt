package com.example.seteasecloudmusic.feature.player.domain

import com.example.seteasecloudmusic.feature.player.data.LyricsRepository
import com.example.seteasecloudmusic.feature.player.domain.model.ParsedLyrics
import javax.inject.Inject

class GetLyricsUseCase @Inject constructor(
    private val lyricsRepository: LyricsRepository
) {
    suspend operator fun invoke(songId: Long): Result<ParsedLyrics> {
        return lyricsRepository.getLyrics(songId)
    }
}
