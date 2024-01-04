package com.entiv.zygonspawner.block

import com.entiv.zygonspawner.data.SpawnerData
import org.bukkit.Location
import org.bukkit.block.CreatureSpawner

class  SpawnerBlock(private val _spawnerData: SpawnerData, val location: Location) {

    val id by spawnerData::id

    val type by _spawnerData::spawnerType
    var totalCount by _spawnerData::totalCount
    val block get() = location.world.getBlockAt(location)
    val spawner get() = block.state as CreatureSpawner
    val spawnerData get() = SpawnerData.fromSpawnerBlock(_spawnerData, spawner)
}
