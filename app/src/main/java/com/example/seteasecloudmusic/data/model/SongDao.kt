package com.example.seteasecloudmusic.data.model

import com.google.gson.JsonElement

data class SongDao(
	val data: List<SongUrlItemDao> = emptyList(),
	val code: Int = 0
)

data class SongUrlItemDao(
	val id: Long = 0L,
	val url: String? = null,
	val br: Int? = null,
	val size: Long? = null,
	val md5: String? = null,
	val code: Int? = null,
	val expi: Int? = null,
	val type: String? = null,
	val gain: Double? = null,
	val peak: Double? = null,
	val closedGain: Double? = null,
	val closedPeak: Double? = null,
	val fee: Int? = null,
	val uf: JsonElement? = null,
	val payed: Int? = null,
	val flag: Long? = null,
	val canExtend: Boolean? = null,
	val freeTrialInfo: JsonElement? = null,
	val level: String? = null,
	val encodeType: String? = null,
	val channelLayout: JsonElement? = null,
	val freeTrialPrivilege: SongFreeTrialPrivilegeDao? = null,
	val freeTimeTrialPrivilege: SongFreeTimeTrialPrivilegeDao? = null,
	val urlSource: Int? = null,
	val rightSource: Int? = null,
	val podcastCtrp: JsonElement? = null,
	val effectTypes: JsonElement? = null,
	val time: Long? = null,
	val message: String? = null,
	val levelConfuse: JsonElement? = null,
	val musicId: String? = null,
	val accompany: JsonElement? = null,
	val sr: Int? = null,
	val auEff: JsonElement? = null,
	val immerseType: JsonElement? = null,
	val beatType: Int? = null
)

data class SongFreeTrialPrivilegeDao(
	val resConsumable: Boolean? = null,
	val userConsumable: Boolean? = null,
	val listenType: Int? = null,
	val cannotListenReason: Int? = null,
	val playReason: String? = null,
	val freeLimitTagType: String? = null
)

data class SongFreeTimeTrialPrivilegeDao(
	val resConsumable: Boolean? = null,
	val userConsumable: Boolean? = null,
	val type: Int? = null,
	val remainTime: Long? = null
)
