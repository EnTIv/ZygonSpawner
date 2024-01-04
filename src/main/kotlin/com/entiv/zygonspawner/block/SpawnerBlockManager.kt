package com.entiv.zygonspawner.block

import com.entiv.core.common.kit.submit
import com.entiv.core.common.module.PluginModule
import com.entiv.core.common.plugin.config
import com.entiv.core.exposed.transaction
import com.entiv.zygonspawner.data.SpawnerData
import com.entiv.zygonspawner.menu.SpawnerInfo
import com.entiv.zygonspawner.storage.SpawnerBlockDao
import com.entiv.zygonspawner.storage.SpawnerBlockEntity
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.CreatureSpawner
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.SpawnerSpawnEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.world.WorldSaveEvent
import org.bukkit.inventory.EquipmentSlot

object SpawnerBlockManager : PluginModule, Listener {

    private val spawnerBlocks = mutableMapOf<Location, SpawnerBlock?>()

    override fun onDisable() {
        SpawnerBlockDao.save(spawnerBlocks.values.filterNotNull()).join()
        spawnerBlocks.clear()
    }

    @EventHandler(ignoreCancelled = true)
    private fun onPlace(event: BlockPlaceEvent) {
        val itemStack = event.itemInHand
        val spawnerData = SpawnerData.fromItemStack(itemStack) ?: return
        val block = event.block
        val spawnerBlock = SpawnerBlock(spawnerData, block.location)

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
        val location = event.spawner.location
        val spawner = findSpawnerBlock(location) ?: return
        spawner.totalCount -= 1

        if (spawner.totalCount <= 0) {
            event.isCancelled = true

            if (config.getBoolean("consume", false)) {
                submit {
                    event.spawner.block.type = Material.AIR
                }
            }
        }
    }

    @EventHandler
    private fun onInteract(event: PlayerInteractEvent) {
        val clickedBlock = event.clickedBlock ?: return
        if (event.hand != EquipmentSlot.HAND) {
            return
        }

        val player = event.player
        val spawner = findSpawnerBlock(clickedBlock.location) ?: return

        SpawnerInfo(spawner.spawnerData).open(player)
    }

    @EventHandler
    private fun onWorldSave(event: WorldSaveEvent) {
        if (event.world == Bukkit.getWorlds().first()) {
            SpawnerBlockDao.save(spawnerBlocks.values.filterNotNull())
        }
    }

    fun findSpawnerBlock(location: Location): SpawnerBlock? {
        if (location.block.state !is CreatureSpawner) {
            return null
        }

        return spawnerBlocks.getOrPut(location) {
            transaction {
                SpawnerBlockEntity.find(location)?.toSpawnerBlock()
            }
        }
    }
}