package com.entiv.zygonspawner.data

import com.entiv.core.common.kit.ItemBuilder
import com.entiv.core.common.message.varTag
import com.entiv.core.common.utils.translatable
import com.entiv.zygonspawner.block.SpawnerBlockManager
import com.entiv.zygonspawner.spawner.SpawnerManager
import de.tr7zw.nbtapi.NBT
import de.tr7zw.nbtapi.iface.ReadWriteNBT
import de.tr7zw.nbtapi.iface.ReadableNBT
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.CreatureSpawner
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta


class SpawnerData(
    val id: String,
    val spawnerType: EntityType,
    var totalCount: Int,
    val minSpawnDelay: Int,
    val maxSpawnDelay: Int,
    val spawnCount: Int,
    val maxNearbyEntities: Int,
    val requiredPlayerRange: Int,
    val spawnRange: Int,
) {

    fun writeToItem(itemStack: ItemStack) {
        NBT.modify(itemStack) {
            writeToNBT(it)
        }
    }

    private fun writeToNBT(nbt: ReadWriteNBT) {
        val compound = nbt.getOrCreateCompound("ZygonSpawner")

        compound.setString("id", id)
        compound.setString("type", spawnerType.name)
        compound.setInteger("totalCount", totalCount)
        compound.setInteger("minSpawnDelay", minSpawnDelay)
        compound.setInteger("maxSpawnDelay", maxSpawnDelay)
        compound.setInteger("spawnCount", spawnCount)
        compound.setInteger("maxNearbyEntities", maxNearbyEntities)
        compound.setInteger("requiredPlayerRange", requiredPlayerRange)
        compound.setInteger("spawnRange", spawnRange)
    }

    fun toItemStack(): ItemStack {
        val zygonSpawner = SpawnerManager.findZygonSpawner(id) ?: error("无法获取到刷怪笼 $id")
        val miniMessage = MiniMessage.miniMessage()
        val name = miniMessage.deserialize(
            zygonSpawner.name,
            varTag("totalCount", totalCount, "type", spawnerType.translatable())
        )
        val lore = zygonSpawner.lore.map {
            miniMessage.deserialize(
                it,
                varTag(
                    "totalCount", totalCount,
                    "type", spawnerType.translatable(),
                    "minSpawnDelay", minSpawnDelay,
                    "maxSpawnDelay", maxSpawnDelay,
                    "spawnCount", spawnCount,
                    "maxNearbyEntities", maxNearbyEntities,
                    "requiredPlayerRange", requiredPlayerRange,
                    "spawnRange", spawnRange
                )
            )
        }

        val itemStack = ItemBuilder(Material.SPAWNER)
            .name(name)
            .lore(lore)
            .build()

        val itemMeta = itemStack.itemMeta
        val blockStateMeta = itemMeta as BlockStateMeta
        val creatureSpawner = blockStateMeta.blockState as CreatureSpawner

        creatureSpawner.spawnedType = spawnerType
        creatureSpawner.minSpawnDelay = minSpawnDelay
        creatureSpawner.maxSpawnDelay = maxSpawnDelay
        creatureSpawner.spawnCount = spawnCount
        creatureSpawner.maxNearbyEntities = maxNearbyEntities
        creatureSpawner.requiredPlayerRange = requiredPlayerRange
        creatureSpawner.spawnRange = spawnRange

        blockStateMeta.blockState = creatureSpawner
        itemStack.itemMeta = itemMeta

        writeToItem(itemStack)

        return itemStack
    }

    companion object {

        private fun fromNBT(nbt: ReadableNBT): SpawnerData? {
            val compound = nbt.getCompound("ZygonSpawner") ?: return null
            val id = compound.getString("id") ?: return null
            val type = compound.getString("type") ?: return null
            val entityType = EntityType.valueOf(type)

            val totalCount = compound.getInteger("totalCount")
            val minSpawnDelay = compound.getInteger("minSpawnDelay")
            val maxSpawnDelay = compound.getInteger("maxSpawnDelay")
            val spawnCount = compound.getInteger("spawnCount")
            val maxNearbyEntities = compound.getInteger("maxNearbyEntities")
            val requiredPlayerRange = compound.getInteger("requiredPlayerRange")
            val spawnRange = compound.getInteger("spawnRange")

            return SpawnerData(
                id, entityType, totalCount, minSpawnDelay, maxSpawnDelay,
                spawnCount, maxNearbyEntities, requiredPlayerRange, spawnRange
            )
        }

        fun fromItemStack(itemStack: ItemStack): SpawnerData? =
            fromNBT(NBT.readNbt(itemStack))

        fun fromLocation(location: Location): SpawnerData? {
            val world = location.world
            val block = world.getBlockAt(location).state as? CreatureSpawner ?: return null

            return fromSpawner(block)
        }

        fun fromSpawner(createSpawner: CreatureSpawner): SpawnerData {
            val spawnerType = createSpawner.spawnedType
            val minSpawnDelay = createSpawner.minSpawnDelay
            val maxSpawnDelay = createSpawner.maxSpawnDelay
            val spawnCount = createSpawner.spawnCount
            val maxNearbyEntities = createSpawner.maxNearbyEntities
            val requiredPlayerRange = createSpawner.requiredPlayerRange
            val spawnRange = createSpawner.spawnRange
            val spawnerBlock = SpawnerBlockManager.findSpawnerBlock(createSpawner.location)!!
            val id = spawnerBlock.spawnerData.id
            val totalCount = spawnerBlock.spawnerData.totalCount

            return SpawnerData(
                id = id,
                spawnerType = spawnerType,
                minSpawnDelay = minSpawnDelay,
                maxSpawnDelay = maxSpawnDelay,
                spawnCount = spawnCount,
                maxNearbyEntities = maxNearbyEntities,
                requiredPlayerRange = requiredPlayerRange,
                spawnRange = spawnRange,
                totalCount = totalCount
            )
        }
    }
}
