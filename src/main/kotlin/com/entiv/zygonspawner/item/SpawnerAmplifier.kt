package com.entiv.zygonspawner.item

import com.entiv.core.common.debug.debug
import com.entiv.core.common.kit.ItemBuilder
import com.entiv.core.common.message.sendWarnMessage
import com.entiv.zygonspawner.block.SpawnerBlock
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class SpawnerAmplifier(
    override val id: String,
    private val itemStack: ItemStack,

    val totalCount: Int,
    val minSpawnDelay: Int,
    val maxSpawnDelay: Int,
    val spawnCount: Int,
    val maxNearbyEntities: Int,
    val requiredPlayerRange: Int,
    val spawnRange: Int
) : Booster {

    override fun getItemStack(): ItemStack {
        return itemStack.clone()
    }

    override fun onInteract(event: PlayerInteractEvent, itemStack: ItemStack, spawnerBlock: SpawnerBlock) {

        val spawner = spawnerBlock.spawner
        val player = event.player

        val newMinSpawnDelay = spawner.minSpawnDelay + minSpawnDelay
        val newMaxSpawnDelay = spawner.maxSpawnDelay + maxSpawnDelay
        val newSpawnCount = spawner.spawnCount + spawnCount
        val newMaxNearbyEntities = spawner.maxNearbyEntities + maxNearbyEntities
        val newRequiredPlayerRange = spawner.requiredPlayerRange + requiredPlayerRange
        val newSpawnRange = spawner.spawnRange + spawnRange

        if (newMinSpawnDelay <= 0) {
            player.sendWarnMessage("最小刷怪间隔不能小于 0")
            return
        }

        if (newMaxSpawnDelay <= 0) {
            player.sendWarnMessage("最大刷怪间隔不能小于 0")
            return
        }

        if (newMaxSpawnDelay < newMinSpawnDelay) {
            player.sendWarnMessage("最大刷怪间隔不能小于最小刷怪间隔")
            return
        }

        if (newSpawnCount < 0) {
            player.sendWarnMessage("刷怪数不能小于0")
            return
        }

        if (newMaxNearbyEntities < 0) {
            player.sendWarnMessage("最大附近实体不能小于0")
            return
        }

        if (requiredPlayerRange < 0) {
            player.sendWarnMessage("激活距离不能小于0")
            return
        }

        if (newSpawnRange < 0) {
            player.sendWarnMessage("刷怪半径不能小于0")
            return
        }

        spawner.minSpawnDelay = newMinSpawnDelay
        spawner.maxSpawnDelay = newMaxSpawnDelay
        spawner.spawnCount = newSpawnCount
        spawner.maxNearbyEntities = newMaxNearbyEntities
        spawner.requiredPlayerRange = newRequiredPlayerRange
        spawner.spawnRange = newSpawnRange
        spawnerBlock.totalCount += totalCount

        itemStack.amount -= 1
    }

    companion object {

        fun registerFromSection(section: ConfigurationSection) {
            section.getKeys(false)
                .forEach {
                    val itemSection = section.getConfigurationSection(it)!!
                    val itemBooster = fromSection(itemSection)

                    BoosterManager.registerItem(itemBooster)
                    debug("已注册 ${section.name} 道具")
                }
        }

        fun fromSection(section: ConfigurationSection): SpawnerAmplifier {
            val id = section.name
            val itemStack = ItemBuilder(section).amount(1).build()

            val totalCount = section.getInt("totalCount", 0)
            val minSpawnDelay = section.getInt("minSpawnDelay", 0)
            val maxSpawnDelay = section.getInt("maxSpawnDelay", 0)
            val spawnCount = section.getInt("spawnCount", 0)
            val maxNearbyEntities = section.getInt("maxNearbyEntities", 0)
            val requiredPlayerRange = section.getInt("requiredPlayerRange", 0)
            val spawnRange = section.getInt("spawnRange", 0)

            return SpawnerAmplifier(
                id,
                itemStack,
                totalCount,
                minSpawnDelay,
                maxSpawnDelay,
                spawnCount,
                maxNearbyEntities,
                requiredPlayerRange,
                spawnRange
            )
        }
    }
}