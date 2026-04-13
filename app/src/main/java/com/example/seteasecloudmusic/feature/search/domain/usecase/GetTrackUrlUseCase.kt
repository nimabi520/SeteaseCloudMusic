package com.example.seteasecloudmusic.feature.search.domain

class GetTrackUrlUseCase(
    private val searchRepository: SearchRepository
) {
    suspend operator fun invoke(trackId: Long, level: String = "hires"): Result<String> {
        if (trackId <= 0L) return Result.failure(IllegalArgumentException("invalid trackId"))
        return searchRepository.getTrackUrl(trackId, level)
    }
}