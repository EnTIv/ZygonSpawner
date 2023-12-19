package com.entiv.zygonspawner.storage

import com.entiv.core.common.plugin.config
import com.entiv.core.exposed.ExposedModule
import org.jetbrains.exposed.sql.Table

object ExposedManager : ExposedModule(config.getConfigurationSection("data")!!) {
    override val tableList get() = setOf(SpawnerBlockTable)
}