package com.entiv.zygonspawner.commands

import com.entiv.core.command.DefaultCommand
import com.entiv.core.command.Options
import com.entiv.core.command.command
import com.entiv.core.common.message.sendSuccessMessage
import com.entiv.zygonspawner.item.BoosterManager
import com.entiv.zygonspawner.spawner.SpawnerManager
import com.entiv.zygonspawner.storage.SpawnerBlockEntity
import org.bukkit.Material
import org.bukkit.block.CreatureSpawner
import org.jetbrains.exposed.sql.transactions.transaction

object BaseCommand {

    val uninstall = command("uninstall") {
        description = "删除所有自定义刷怪笼"
        parent(DefaultCommand.root)
        var lastExecutionTime: Long? = null
        exec {
            val currentTime = System.currentTimeMillis()
            val second = 5

            if (lastExecutionTime != null && currentTime - lastExecutionTime!! <= second * 1000) {
                transaction {
                    val spawnerBlockEntities = SpawnerBlockEntity.all()
                    spawnerBlockEntities.forEach {
                        val block = it.location.block
                        if (block.state !is CreatureSpawner) return@forEach
                        block.type = Material.AIR
                    }
                    tellSuccess("已经移除了 %0 个自定义刷怪笼", spawnerBlockEntities.count())
                }
                lastExecutionTime = null
            } else {
                lastExecutionTime = currentTime
                tellWarn("请在 %0 秒内再次执行命令以确认删除所有刷怪笼", second)
            }
        }
    }

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

            sender.sendSuccessMessage("给予玩家 %0 刷怪笼 %1 数量 %2 个成功", target.name, zygonSpawner.id, amount)
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

            sender.sendSuccessMessage("给予玩家 %0 刷怪笼增幅器 %1 数量 %2 个成功", target.name, booster.id, amount)
        }
    }

    fun register() {
        uninstall.register()
        giveBooster.register()
        giveSpawner.register()
    }
}