package com.entiv.zygonspawner


import com.entiv.core.command.DefaultCommand
import com.entiv.core.common.message.sendMessage
import com.entiv.core.common.module.ModuleManager
import com.entiv.core.common.plugin.InsekiPlugin
import com.entiv.zygonspawner.commands.GiveCommand
import com.entiv.zygonspawner.spawner.SpawnerManager
import org.bukkit.Bukkit

class ZygonSpawnerPlugin : InsekiPlugin() {

    override fun onEnabled() {
        sendAdvertisement(true)

        DefaultCommand.register()
        GiveCommand.give.register()

        ModuleManager.load(SpawnerManager)
    }

    override fun onDisabled() {
        sendAdvertisement(false)
    }
}