package com.entiv.zygonspawner

import com.entiv.core.common.kit.ItemBuilder
import com.entiv.zygonspawner.spawner.SpawnerManager
import de.tr7zw.nbtapi.NBT
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemStack

class SpawnerData(val id: String, val type: EntityType, val count: Int) {
    fun writeToItemStack(itemStack: ItemStack) {
        NBT.modify(itemStack) {
            val compound = it.getOrCreateCompound("ZygonSpawner")

            compound.setString("id", id)
            compound.setString("type", type.name)
            compound.setInteger("count", count)
        }
    }

    fun writeToBlock(block: Block) {
        val state = block.state

        NBT.modify(state) {
            val compound = it.getOrCreateCompound("ZygonSpawner")

            compound.setString("id", id)
            compound.setString("type", type.name)
            compound.setInteger("count", count)
        }
    }

    fun toItemStack(): ItemStack {
        val zygonSpawner = SpawnerManager.findZygonSpawner(id) ?: error("无法获取到刷怪笼 $id")

        val itemStack = ItemBuilder(Material.SPAWNER)
            .name(zygonSpawner.name)
            .lore(zygonSpawner.lore)
            .build()

        TagResolver.re
        //TODO lore 替换变量
        writeToItemStack(itemStack)

        return itemStack
    }

    companion object {
        fun fromItemStack(itemStack: ItemStack): SpawnerData? {
            var spawnerData: SpawnerData? = null

            NBT.get(itemStack) {
                val compound = it.getCompound("ZygonSpawner") ?: return@get

                val id = compound.getString("id") ?: return@get
                val type = compound.getString("type") ?: return@get
                val entityType = EntityType.valueOf(type)
                val count = compound.getInteger("count")

                spawnerData = SpawnerData(id, entityType, count)
            }

            return spawnerData
        }

        fun fromBlock(block: Block): SpawnerData? {
            var spawnerData: SpawnerData? = null

            NBT.get(block.state) {
                val id = it.getString("id") ?: return@get
                val type = it.getString("type") ?: return@get

                val entityType = EntityType.valueOf(type)
                val count = it.getInteger("count")

                spawnerData = SpawnerData(id, entityType, count)
            }

            return spawnerData
        }
    }
}