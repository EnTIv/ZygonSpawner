package com.entiv.zygonspawner

import com.entiv.core.common.kit.ItemBuilder
import com.entiv.core.common.message.varTag
import com.entiv.zygonspawner.spawner.SpawnerManager
import de.tr7zw.nbtapi.NBT
import de.tr7zw.nbtapi.NBTCompound
import de.tr7zw.nbtapi.NBTTileEntity
import de.tr7zw.nbtapi.iface.ReadWriteNBT
import de.tr7zw.nbtapi.iface.ReadableNBT
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockState
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemStack

class SpawnerData(val id: String, val type: EntityType, val count: Int) {

    fun writeToItem(itemStack: ItemStack) {
        NBT.modify(itemStack) {
            writeToNBT(it)
        }
    }

    fun writeToBlock(blockState: BlockState) {
        NBT.modify(blockState) {
            writeToNBT(it)
        }
    }

    private fun writeToNBT(nbt: ReadWriteNBT) {
        val compound = nbt.getOrCreateCompound("ZygonSpawner")
        compound.setString("id", id)
        compound.setString("type", type.name)
        compound.setInteger("count", count)
    }

    fun toItemStack(): ItemStack {
        val zygonSpawner = SpawnerManager.findZygonSpawner(id) ?: error("无法获取到刷怪笼 $id")
        val miniMessage = MiniMessage.miniMessage()
        val name = miniMessage.deserialize(zygonSpawner.name)
        val lore = zygonSpawner.lore.map {
            miniMessage.deserialize(it, varTag("耐久", count, "类型", type))
        }

        val itemStack = ItemBuilder(Material.SPAWNER)
            .name(name)
            .lore(lore)
            .build()

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

        fun fromBlock(block: Block): SpawnerData? =
            fromNBT(NBTTileEntity(block.state))
    }
}
