package com.entiv.zygonspawner

import de.tr7zw.nbtapi.NBT
import org.bukkit.block.Block
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemStack

class SpawnerData(val type: EntityType, val count: Int) {

    fun writeToItemStack(itemStack: ItemStack) {
        NBT.modify(itemStack) {
            it.setString("type", type.name)
            it.setInteger("count", count)
        }
    }

    fun writeToBlock(block: Block) {
        val state = block.state

        NBT.modify(state) {
            it.setString("type", type.name)
            it.setInteger("count", count)
        }
    }

    companion object {
        fun fromItemStack(itemStack: ItemStack): SpawnerData? {
            var spawnerData: SpawnerData? = null

            NBT.get(itemStack) {
                val type = it.getString("type") ?: return@get
                val entityType = EntityType.valueOf(type)
                val count = it.getInteger("count")

                spawnerData = SpawnerData(entityType, count)
            }

            return spawnerData
        }

        fun fromBlock(block: Block): SpawnerData? {
            var spawnerData: SpawnerData? = null

            NBT.get(block.state) {
                val type = it.getString("type") ?: return@get
                val entityType = EntityType.valueOf(type)
                val count = it.getInteger("count")

                spawnerData = SpawnerData(entityType, count)
            }

            return spawnerData
        }
    }

}