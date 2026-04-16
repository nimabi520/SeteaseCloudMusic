package com.example.seteasecloudmusic.feature.artist.data

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Artist related HTTP API declarations.
 */
interface ArtistService {
    @GET("artist/songs")
    suspend fun getArtistSongs(
        @Query("id") artistId: Long,
        @Query("order") order: String = "hot",
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): ArtistSongsResponse

    @GET("artist/album")
    suspend fun getArtistAlbums(
        @Query("id") artistId: Long,
        @Query("limit") limit: Int = 30,
        @Query("offset") offset: Int = 0
    ): ArtistAlbumsResponse

    @GET("artist/desc")
    suspend fun getArtistDescription(
        @Query("id") artistId: Long
    ): ArtistDescriptionResponse

    @GET("artist/detail")
    suspend fun getArtistDetail(
        @Query("id") artistId: Long
    ): ArtistDetailResponse

    @GET("simi/artist")
    suspend fun getSimilarArtists(
        @Query("id") artistId: Long
    ): SimilarArtistsResponse
}
