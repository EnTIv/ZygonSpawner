package com.entiv.zygonspawner.commands

import com.entiv.core.command.DefaultCommand
import com.entiv.core.command.Options
import com.entiv.core.command.command
import com.entiv.core.common.message.sendSuccessMessage
import com.entiv.zygonspawner.item.Booster
import com.entiv.zygonspawner.item.BoosterManager
import com.entiv.zygonspawner.spawner.SpawnerManager

// /ZygonSpawner give 类型 数量 玩家
object GiveCommand {

    val giveSpawner = command("give-spawner") {
        description = "给予玩家指定类型的刷怪笼"
        parent(DefaultCommand.root)
        arg("<玩家>") { Options.onlinePlayer }
        arg("<类型>") { SpawnerManager.getZygonSpawners().map { it.id } }
        arg("[数量]", Options.integer)
        exec {
            val target = findPlayer(0) ?: error("请输入玩家名")
            val zygonSpawner = findString(1)?.let {
                SpawnerManager.findZygonSpawner(it) ?: error("找不到刷怪笼 $it")
            } ?: error("请输入刷怪笼类型")
            val amount = findInt(2) ?: 1

            val itemStack = zygonSpawner.generateData().toItemStack(amount)
            target.inventory.addItem(itemStack)

            sender.sendSuccessMessage("给予玩家 ${target.name} 刷怪笼 ${zygonSpawner.id} $amount 个成功")
        }
    }

    val giveBooster = command("give-booster") {
        description = "给予玩家指定类型的刷怪笼增幅器"
        parent(DefaultCommand.root)
        arg("<玩家>") { Options.onlinePlayer }
        arg("<类型>") { BoosterManager.getBoosters().map { it.id } }
        arg("[数量]", Options.integer)
        exec {
            val target = findPlayer(0) ?: error("请输入玩家名")
            val booster = findString(1)?.let {
                BoosterManager.findBooster(it) ?: error("找不到刷怪笼增幅器 $it")
            } ?: error("请输入增幅器类型")

            val amount = findInt(2) ?: 1

            val itemStack = booster.getItemStack()
            target.inventory.addItem(itemStack)

            sender.sendSuccessMessage("给予玩家 ${target.name} 刷怪笼 ${booster.id} $amount 个成功")
        }
    }

    fun register() {
        giveBooster.register()
        giveSpawner.register()
    }
}