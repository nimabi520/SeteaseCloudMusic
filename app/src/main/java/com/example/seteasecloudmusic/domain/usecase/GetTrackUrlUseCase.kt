package com.example.seteasecloudmusic.domain.usecase

import com.example.seteasecloudmusic.domain.repository.MusicRepository

class GetTrackUrlUseCase(
    private val musicRepository: MusicRepository
) {
    suspend operator fun invoke(trackId: Long, level: String = "hires"): Result<String> {
        if (trackId <= 0L) return Result.failure(IllegalArgumentException("invalid trackId"))
        return musicRepository.getTrackUrl(trackId, level)
    }
}