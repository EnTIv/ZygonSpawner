package com.entiv.zygonspawner.block

import com.entiv.zygonspawner.data.SpawnerData
import org.bukkit.Location
import org.bukkit.block.CreatureSpawner

class SpawnerBlock(spawnerData: SpawnerData, val location: Location) {

    val spawnerData get() =  SpawnerData.fromLocation(location)!!
    val id by spawnerData::id
    val type by spawnerData::spawnerType
    var totalCount by spawnerData::totalCount
    val block get() = location.world.getBlockAt(location)

    val spawner: CreatureSpawner get() {
        return block.state as CreatureSpawner
    }
}