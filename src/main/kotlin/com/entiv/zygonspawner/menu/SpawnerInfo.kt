package com.entiv.zygonspawner.menu

import com.entiv.core.menu.SimpleMenu
import com.entiv.zygonspawner.data.SpawnerData
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.impl.SimpleItem

class SpawnerInfo(private val spawnerData: SpawnerData) : SimpleMenu() {
    override val gui = Gui.empty(9, 1)
    override val title = "刷怪笼信息"

    init {
        val infoItem = object : SimpleItem(spawnerData.toItemStack()) {}
        gui.setItem(4, infoItem)
        val backgroundItem = SimpleItem(ItemStack(Material.BLUE_STAINED_GLASS_PANE)) {}

        gui.fillRow(0, backgroundItem, false)
    }

}