package com.entiv.zygonspawner

import com.entiv.core.module.PluginModule
import com.entiv.core.plugin.plugin
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.SpawnerSpawnEvent
import org.bukkit.persistence.PersistentDataType

object SpawnerManager : PluginModule, Listener {


    @EventHandler
    private fun onPlace(event: BlockPlaceEvent) {
        val itemStack = event.itemInHand
        val spawnerData = SpawnerData.fromItemStack(itemStack) ?: return
        val block = event.block

        spawnerData.writeToBlock(block)
    }

    @EventHandler
    private fun onBreak(event: BlockBreakEvent) {
        val block = event.block
        val spawnerData = SpawnerData.fromBlock(block) ?: return

        event.isCancelled = true
    }

    @EventHandler
    private fun onSummon(event: SpawnerSpawnEvent) {
        val spawner = event.spawner

        val dataContainer = spawner.persistentDataContainer
        val countKey = NamespacedKey(plugin, "count")
        dataContainer.set(countKey, PersistentDataType.INTEGER, 5)
        val remainingCount = dataContainer.get(countKey, PersistentDataType.INTEGER)
    }
}