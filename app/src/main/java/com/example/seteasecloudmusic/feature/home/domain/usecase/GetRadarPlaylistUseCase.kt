package com.example.seteasecloudmusic.feature.home.domain.usecase

import com.example.seteasecloudmusic.feature.home.domain.repository.HomeRecommendRepository
import com.example.seteasecloudmusic.feature.home.domain.repository.RadarPlaylistData
import javax.inject.Inject

class GetRadarPlaylistUseCase @Inject constructor(
    private val homeRecommendRepository: HomeRecommendRepository
) {
    suspend operator fun invoke(): Result<RadarPlaylistData> {
        return homeRecommendRepository.getRadarPlaylist()
    }

    fun getCached(): RadarPlaylistData? {
        return homeRecommendRepository.getCachedRadarPlaylist()
    }
}
