package com.entiv.zygonspawner.item

import com.entiv.zygonspawner.block.SpawnerBlock
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

interface Booster {
    val id: String

    fun getItemStack(): ItemStack

    fun onInteract(event: PlayerInteractEvent, itemStack: ItemStack, spawnerBlock: SpawnerBlock)
}