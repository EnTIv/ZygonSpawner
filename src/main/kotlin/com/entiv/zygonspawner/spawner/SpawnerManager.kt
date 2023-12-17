package com.entiv.zygonspawner.spawner

import com.entiv.core.common.debug.debug
import com.entiv.core.common.module.PluginModule
import com.entiv.core.common.plugin.config
import com.entiv.core.common.plugin.plugin
import com.entiv.zygonspawner.SpawnerData
import org.bukkit.block.TileState
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.SpawnerSpawnEvent
import org.bukkit.metadata.FixedMetadataValue

object SpawnerManager : PluginModule, Listener {

    private val spawners = mutableMapOf<String, ZygonSpawner>()

    override fun onEnable() {
        val section = config.getConfigurationSection("刷怪笼") ?: error("找不到刷怪笼配置")

        section.getKeys(false)
            .mapNotNull { section.getConfigurationSection(it) }
            .map { ZygonSpawner.fromSection(it) }
            .forEach {
                spawners[it.id] = it
                debug("刷怪笼生成配置 ${it.id} 已加载")
            }
    }
    override fun onDisable() {
        spawners.clear()
    }

    fun generatorSpawnData(id: String): SpawnerData? {
        return spawners[id]?.generateData()
    }

    fun findZygonSpawner(id: String): ZygonSpawner? {
        return spawners[id]
    }

    fun getZygonSpawner(id: String): ZygonSpawner {
        return spawners[id] ?: error("刷怪笼 $id 不存在")
    }

    fun getZygonSpawners() = spawners.values

    @EventHandler(ignoreCancelled = true)
    private fun onPlace(event: BlockPlaceEvent) {
        val itemStack = event.itemInHand

        val spawnerData = SpawnerData.fromItemStack(itemStack) ?: return
        val state = event.block.state as? TileState ?: return

        event.block.state.setMetadata("count", FixedMetadataValue(plugin, 10))
        spawnerData.writeToBlock(state)
    }

    @EventHandler(ignoreCancelled = true)
    private fun onBreak(event: BlockBreakEvent) {
        val block = event.block

        val state = block.state as? TileState ?: return
        val spawnerData = SpawnerData.fromBlockState(state) ?: return
        val itemStack = spawnerData.toItemStack()


        val world = block.world
        world.dropItem(block.location, itemStack)
        event.isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    private fun onSummon(event: SpawnerSpawnEvent) {
        val spawner = event.spawner
        println(spawner.getMetadata("count")[0].asInt())

        val spawnerData = SpawnerData.fromBlockState(spawner) ?: return


        spawnerData.count -= 1
        spawnerData.writeToBlock(spawner)
    }

}