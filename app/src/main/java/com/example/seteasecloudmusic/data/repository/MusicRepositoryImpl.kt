package com.example.seteasecloudmusic.data.repository

import com.example.seteasecloudmusic.data.api.NeteaseMusicService
import com.example.seteasecloudmusic.data.model.SearchSongItemResponse
import com.example.seteasecloudmusic.domain.model.Album
import com.example.seteasecloudmusic.domain.model.Artist
import com.example.seteasecloudmusic.domain.model.AudioQuality
import com.example.seteasecloudmusic.domain.model.Track
import com.example.seteasecloudmusic.domain.repository.MusicRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MusicRepositoryImpl(
    private val musicService: NeteaseMusicService
) : MusicRepository {

    override suspend fun searchTracks(
        query: String,
        limit: Int,
        offset: Int
    ): Result<List<Track>> = withContext(Dispatchers.IO) {
        try {
            val response = musicService.searchSongs(query, limit, offset)
            if (response.code == 200) {
                val tracks = response.result?.songs?.map { song ->
                    mapToDomainTrack(song)
                } ?: emptyList()
                Result.success(tracks)
            } else {
                Result.failure(Exception("API Error with code: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTrackUrl(
        trackId: Long,
        level: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = musicService.getSongUrl(trackId, level)
            if (response.code == 200) {
                val url = response.data.firstOrNull()?.url
                if (url != null) {
                    Result.success(url)
                } else {
                    Result.failure(Exception("URL parameter is null"))
                }
            } else {
                Result.failure(Exception("API Error with code: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 内部映射函数：将网络的 SearchSongItemResponse 转换为领域的 Track。
     */
    private fun mapToDomainTrack(song: SearchSongItemResponse): Track {
        // 提取音质标签
        val qualityTags = mutableListOf<AudioQuality>()
        if (song.sq != null) qualityTags.add(AudioQuality.LOSSLESS)
        if (song.hr != null) qualityTags.add(AudioQuality.HIRES)
        if (song.h != null) qualityTags.add(AudioQuality.HIGH)
        if (song.l != null || song.m != null) qualityTags.add(AudioQuality.STANDARD)

        return Track(
            id = song.id,
            title = song.name,
            durationMs = song.dt,
            artists = song.ar.map { artist ->
                Artist(
                    id = artist.id ?: 0L,
                    name = artist.name ?: "未知歌手",
                    coverUrl = null // 搜索接口一般不返回歌手图，所以这里给空
                )
            },
            album = Album(
                id = song.al?.id ?: 0L,
                title = song.al?.name ?: "未知专辑",
                // 网易云返回的图片 URL (部分可能叫 picUrl 或 pic_str)
                coverUrl = song.al?.picUrl
            ),
            // Track 本身也保留一份专辑封面作为歌曲封面，方便 UI 直接读取
            coverUrl = song.al?.picUrl,
            qualityTags = qualityTags,
            // 如果后续有 URL 可以更新到这里
            playableUrl = null,
            // 可以通过判断 privilege 层级赋予，如果没有 fee，也可以先假定可播
            isPlayable = song.fee != 1 && song.fee != 4 
        )
    }
} 