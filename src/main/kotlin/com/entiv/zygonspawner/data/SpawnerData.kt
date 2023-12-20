package com.entiv.zygonspawner.data

import com.entiv.core.common.kit.ItemBuilder
import com.entiv.core.common.message.varTag
import com.entiv.core.common.utils.translatable
import com.entiv.zygonspawner.spawner.SpawnerManager
import de.tr7zw.nbtapi.NBT
import de.tr7zw.nbtapi.NBTTileEntity
import de.tr7zw.nbtapi.iface.ReadWriteNBT
import de.tr7zw.nbtapi.iface.ReadableNBT
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.block.CreatureSpawner
import org.bukkit.block.TileState
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta


class SpawnerData(val name: String, val type: EntityType, var count: Int) {

    fun writeToItem(itemStack: ItemStack) {
        NBT.modify(itemStack) {
            writeToNBT(it)
        }
    }

    fun writeToBlock(blockState: TileState) {
        NBT.modify(blockState) {
            writeToNBT(it)
        }
    }

    private fun writeToNBT(nbt: ReadWriteNBT) {
        val compound = nbt.getOrCreateCompound("ZygonSpawner")
        compound.setString("id", name)
        compound.setString("type", type.name)
        compound.setInteger("count", count)
    }

    fun toItemStack(): ItemStack {
        val zygonSpawner = SpawnerManager.findZygonSpawner(name) ?: error("无法获取到刷怪笼 $name")
        val miniMessage = MiniMessage.miniMessage()
        val name = miniMessage.deserialize(zygonSpawner.name, varTag("刷怪次数", count, "类型", type))
        val lore = zygonSpawner.lore.map {
            miniMessage.deserialize(it, varTag("刷怪次数", count, "类型", Component.translatable(type.translationKey())))
        }

        val itemStack = ItemBuilder(Material.SPAWNER)
            .name(name)
            .lore(lore)
            .build()

        val itemMeta = itemStack.itemMeta
        val blockStateMeta = itemMeta as BlockStateMeta
        val creatureSpawner = blockStateMeta.blockState as CreatureSpawner

        creatureSpawner.spawnedType = type
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
            val count = compound.getInteger("count")

            return SpawnerData(id, entityType, count)
        }

        fun fromItemStack(itemStack: ItemStack): SpawnerData? =
            fromNBT(NBT.readNbt(itemStack))

        fun fromBlockState(state: TileState): SpawnerData? =
            fromNBT(NBTTileEntity(state))
    }
}
