package com.example.seteasecloudmusic.feature.artist.domain.model

data class ArtistDetailInfo(
    val id: Long,
    val name: String,
    val coverUrl: String?,
    val musicCount: Int?,
    val albumCount: Int?,
    val mvCount: Int?
)

data class ArtistDescription(
    val brief: String,
    val sections: List<ArtistDescriptionSection>
)

data class ArtistDescriptionSection(
    val title: String,
    val content: String
)

data class ArtistAlbum(
    val id: Long,
    val title: String,
    val coverUrl: String?,
    val publishYear: Int?,
    val trackCount: Int?
)

data class ArtistSummary(
    val id: Long,
    val name: String,
    val coverUrl: String?
)

enum class ArtistSongOrder(val value: String) {
    HOT("hot"),
    TIME("time")
}

data class PagedResult<T>(
    val items: List<T>,
    val nextOffset: Int,
    val hasMore: Boolean
)
