package com.entiv.zygonspawner.block

import com.entiv.zygonspawner.data.SpawnerData
import org.bukkit.Location

class SpawnerBlock(val spawnerData: SpawnerData, val location: Location) {
    val name by spawnerData::name
    val type by spawnerData::type
    var totalCount by spawnerData::totalCount
}