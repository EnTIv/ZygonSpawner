package com.entiv.zygonspawner.item

import com.entiv.core.common.module.PluginModule
import com.entiv.core.common.plugin.config
import com.entiv.zygonspawner.block.SpawnerBlockManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

object BoosterManager : PluginModule, Listener {

    private val boosters = mutableMapOf<String, Booster>()

    override fun onEnable() {
        val section = config.getConfigurationSection("booster-item")!!
        SpawnerAmplifier.registerFromSection(section)
    }

    override fun onDisable() {
        boosters.clear()
    }

    @EventHandler
    private fun onInteract(event: PlayerInteractEvent) {
        val clickedBlock = event.clickedBlock ?: return
        val spawnerBlock = SpawnerBlockManager.findSpawnerBlock(clickedBlock.location) ?: return

        val itemStack = event.item ?: return
        val item = findItem(itemStack) ?: return

        item.onInteract(event, itemStack, spawnerBlock)
    }

    fun findItem(itemStack: ItemStack): Booster? {
        boosters.values.forEach {
            if (it.itemStack.isSimilar(itemStack)) {
                return it
            }
        }
        return null
    }

    fun registerItem(booster: Booster) {
        boosters[booster.id] = booster
    }
}