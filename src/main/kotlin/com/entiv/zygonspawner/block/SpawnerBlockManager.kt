package com.entiv.zygonspawner.block

import com.entiv.core.common.debug.debug
import com.entiv.core.common.module.PluginModule
import com.entiv.zygonspawner.data.SpawnerData
import com.entiv.zygonspawner.storage.SpawnerBlockDao
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.SpawnerSpawnEvent
import org.bukkit.event.world.WorldSaveEvent

object SpawnerBlockManager : PluginModule, Listener {

    private val spawnerBlocks = mutableMapOf<Location, SpawnerBlock>()

    override fun onEnable() {
        SpawnerBlockDao.loadAll().thenAccept {
            it.forEach { spawnerBlock ->

                spawnerBlocks[spawnerBlock.location] = spawnerBlock
            }
        }
    }

    override fun onDisable() {
        SpawnerBlockDao.save(spawnerBlocks.values).join()
        spawnerBlocks.clear()
    }

    @EventHandler(ignoreCancelled = true)
    private fun onPlace(event: BlockPlaceEvent) {
        val itemStack = event.itemInHand
        debug("使用物品 $itemStack")
        val spawnerData = SpawnerData.fromItemStack(itemStack) ?: return
        debug("结果 $spawnerData")
        val block = event.block
        val spawnerBlock = SpawnerBlock(spawnerData, block.location)

        debug("已放置刷怪笼")
        spawnerBlocks[block.location] = spawnerBlock
    }

    @EventHandler(ignoreCancelled = true)
    private fun onBreak(event: BlockBreakEvent) {
        val block = event.block
        val location = block.location
        val spawnerBlock = findSpawnerBlock(location) ?: return
        val itemStack = spawnerBlock.spawnerData.toItemStack()

        location.world.dropItem(location, itemStack)
        block.setType(Material.AIR, true)
        event.isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    private fun onSummon(event: SpawnerSpawnEvent) {
        val spawner = findSpawnerBlock(event.spawner.location) ?: return
        val data = spawner.spawnerData

        debug("刷怪笼计数 ${data.count} - 1")
        data.count -= 1

        if (data.count == 0) {
            event.spawner.type = Material.AIR
        }
    }

    @EventHandler
    private fun onWorldSave(event: WorldSaveEvent) {
        if (event.world == Bukkit.getWorlds().first()) {
            SpawnerBlockDao.save(spawnerBlocks.values)
        }
    }

    fun findSpawnerBlock(location: Location): SpawnerBlock? {
        return spawnerBlocks[location]
    }
}