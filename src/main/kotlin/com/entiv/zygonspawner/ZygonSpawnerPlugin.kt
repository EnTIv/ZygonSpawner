package com.entiv.zygonspawner

import com.entiv.core.command.DefaultCommand
import com.entiv.core.common.module.ModuleManager
import com.entiv.core.common.plugin.InsekiPlugin
import com.entiv.zygonspawner.block.SpawnerBlockManager
import com.entiv.zygonspawner.commands.GiveCommand
import com.entiv.zygonspawner.item.BoosterManager
import com.entiv.zygonspawner.spawner.SpawnerManager
import com.entiv.zygonspawner.storage.ExposedManager

class ZygonSpawnerPlugin : InsekiPlugin() {
    override fun onEnabled() {
        saveDefaultConfig(false)
        sendAdvertisement(true)

        DefaultCommand.register()
        GiveCommand.register()

        ModuleManager.load(ExposedManager)
        ModuleManager.load(SpawnerManager)
        ModuleManager.load(SpawnerBlockManager)
        ModuleManager.load(BoosterManager)
    }

    override fun onDisabled() {
        sendAdvertisement(false)
    }
}