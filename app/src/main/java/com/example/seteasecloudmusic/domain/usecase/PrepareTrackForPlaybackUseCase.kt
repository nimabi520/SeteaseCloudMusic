package com.example.seteasecloudmusic.domain.usecase

import com.example.seteasecloudmusic.domain.model.Track

class PrepareTrackForPlaybackUseCase (
    private val getTrackUrlUseCase: GetTrackUrlUseCase
) {
    suspend operator fun invoke(track: Track): Result<Track> {
        return getTrackUrlUseCase(track.id).map { url ->
            track.copy(playableUrl = url, isPlayable = url.isNotBlank())
        }
    }
}