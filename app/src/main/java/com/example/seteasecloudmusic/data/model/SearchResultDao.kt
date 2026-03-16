package com.example.seteasecloudmusic.data.model

import com.google.gson.annotations.SerializedName

/**
 * cloudsearch/search 接口的顶层响应。
 */
data class SearchResultDao(
	val result: SearchResultBodyDao? = null,
	val code: Int = 0,
	val trp: SearchTrackPathDao? = null
)

/**
 * 搜索结果主体，包含歌曲列表与总数。
 */
data class SearchResultBodyDao(
	val songs: List<SearchSongItemDao> = emptyList(),
	val songCount: Int = 0,
	val hasMore: Boolean? = null
)

/**
 * 单首歌曲的搜索结果条目。
 */
data class SearchSongItemDao(
	val no: Int = 0,
	val rt: String? = null,
	val copyright: Int? = null,
	val fee: Int? = null,
	val privilege: SongPrivilegeDao? = null,
	val mst: Int? = null,
	val pst: Int? = null,
	val pop: Int? = null,
	val dt: Long? = null,
	val rtype: Int? = null,
	@SerializedName("s_id")
	val songAliasId: Long? = null,
	val rtUrls: List<String> = emptyList(),
	val resourceState: Boolean? = null,
	val id: Long = 0L,
	val sq: SongQualityDao? = null,
	val hr: SongQualityDao? = null,
	val st: Int? = null,
	val cd: String? = null,
	val publishTime: Long? = null,
	val cf: String? = null,
	val originCoverType: Int? = null,
	val h: SongQualityDao? = null,
	val mv: Long? = null,
	val al: SearchAlbumDao? = null,
	val originSongSimpleData: OriginSongSimpleDataDao? = null,
	val l: SongQualityDao? = null,
	val m: SongQualityDao? = null,
	val version: Int? = null,
	val cp: Int? = null,
	val alia: List<String> = emptyList(),
	val djId: Long? = null,
	val single: Int? = null,
	val ar: List<SearchArtistDao> = emptyList(),
	val ftype: Int? = null,
	val t: Int? = null,
	val v: Int? = null,
	val name: String = "",
	val mark: Long? = null
)

/**
 * 歌曲权限信息（可播、可下、码率等级等）。
 */
data class SongPrivilegeDao(
	val flag: Long? = null,
	val code: Int? = null,
	val dlLevel: String? = null,
	val subp: Int? = null,
	val fl: Long? = null,
	val fee: Int? = null,
	val dl: Long? = null,
	val plLevel: String? = null,
	val maxBrLevel: String? = null,
	val rightSource: Int? = null,
	val maxbr: Long? = null,
	val id: Long? = null,
	val sp: Int? = null,
	val payed: Int? = null,
	val st: Int? = null,
	val chargeInfoList: List<ChargeInfoDao> = emptyList(),
	val freeTrialPrivilege: FreeTrialPrivilegeDao? = null,
	val downloadMaxbr: Long? = null,
	val downloadMaxBrLevel: String? = null,
	val cp: Int? = null,
	val preSell: Boolean? = null,
	val playMaxBrLevel: String? = null,
	val cs: Boolean? = null,
	val toast: Boolean? = null,
	val playMaxbr: Long? = null,
	val flLevel: String? = null,
	val pl: Long? = null
)

/**
 * 权益计费信息条目。
 */
data class ChargeInfoDao(
	val rate: Long? = null,
	val chargeType: Int? = null
)

/**
 * 免费试听权限信息。
 */
data class FreeTrialPrivilegeDao(
	val userConsumable: Boolean? = null,
	val resConsumable: Boolean? = null,
	val cannotListenReason: Int? = null,
	val listenType: Int? = null
)

/**
 * 音质信息（码率、采样率、文件大小等）。
 */
data class SongQualityDao(
	val br: Long? = null,
	val fid: Long? = null,
	val size: Long? = null,
	val vd: Long? = null,
	val sr: Int? = null
)

/**
 * 专辑信息。
 */
data class SearchAlbumDao(
	val picUrl: String? = null,
	val name: String? = null,
	val tns: List<String> = emptyList(),
	@SerializedName("pic_str")
	val picStr: String? = null,
	val id: Long? = null,
	val pic: Long? = null
)

/**
 * 歌手信息。
 */
data class SearchArtistDao(
	val name: String? = null,
	val tns: List<String> = emptyList(),
	val alias: List<String> = emptyList(),
	val id: Long? = null,
	val alia: List<String> = emptyList()
)

/**
 * 改编/关联原曲信息。
 */
data class OriginSongSimpleDataDao(
	val artists: List<OriginSongArtistDao> = emptyList(),
	val name: String? = null,
	val songId: Long? = null,
	val albumMeta: OriginSongAlbumMetaDao? = null
)

/**
 * 原曲歌手信息。
 */
data class OriginSongArtistDao(
	val name: String? = null,
	val id: Long? = null
)

/**
 * 原曲专辑信息。
 */
data class OriginSongAlbumMetaDao(
	val name: String? = null,
	val id: Long? = null
)

/**
 * 搜索链路规则信息（实验/策略字段）。
 */
data class SearchTrackPathDao(
	val rules: List<String> = emptyList()
)
