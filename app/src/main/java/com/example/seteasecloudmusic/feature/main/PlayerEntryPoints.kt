package com.example.seteasecloudmusic.feature.main

import com.example.seteasecloudmusic.core.player.MusicPlayerController

object PlayerEntryPoints {
    // 由应用启动处注入
    private var controller: MusicPlayerController? = null

    fun init(playerController: MusicPlayerController) {
        controller = playerController
    }

    fun playerController(): MusicPlayerController {
        return requireNotNull(controller) {
            "PlayerEntryPoints is not initialized. Call PlayerEntryPoints.init(...) first."
        }
    }
}