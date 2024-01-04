package com.entiv.zygonspawner.storage

import com.entiv.core.common.debug.debug
import com.entiv.core.exposed.defaultTableName
import com.entiv.core.exposed.entityType
import com.entiv.core.exposed.location
import com.entiv.core.exposed.transaction
import com.entiv.zygonspawner.block.SpawnerBlock
import com.entiv.zygonspawner.data.SpawnerData
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.CreatureSpawner
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

import org.jetbrains.exposed.dao.id.IntIdTable
import java.util.concurrent.CompletableFuture

object SpawnerBlockTable : IntIdTable() {
    override val tableName = defaultTableName()

    val name = varchar("name", 32)
    val entityType = entityType("entityType")
    val totalCount = integer("totalCount")
    val location = location("location").uniqueIndex()
}

class SpawnerBlockEntity(id: EntityID<Int>) : IntEntity(id) {
    var name by SpawnerBlockTable.name
    var type by SpawnerBlockTable.entityType
    var totalCount by SpawnerBlockTable.totalCount
    var location by SpawnerBlockTable.location

    fun toSpawnerBlock(): SpawnerBlock? {
        val spawner = location.block.state as? CreatureSpawner ?: let {
            it.delete()
            return null
        }

        if (totalCount <= 0) {
            delete()
            return null
        }

        val spawnerData = SpawnerData(
            id = name,
            spawnerType = type,
            totalCount = totalCount,
            minSpawnDelay = spawner.minSpawnDelay,
            maxSpawnDelay = spawner.maxSpawnDelay,
            spawnCount = spawner.spawnCount,
            maxNearbyEntities = spawner.maxNearbyEntities,
            requiredPlayerRange = spawner.requiredPlayerRange,
            spawnRange = spawner.spawnRange
        )
        return SpawnerBlock(spawnerData, location)
    }

    companion object : IntEntityClass<SpawnerBlockEntity>(SpawnerBlockTable) {
        fun find(location: Location): SpawnerBlockEntity? {
            return find { SpawnerBlockTable.location eq location }.singleOrNull()
        }

        fun create(spawnerBlock: SpawnerBlock): SpawnerBlockEntity {
            return SpawnerBlockEntity.new {
                this.name = spawnerBlock.id
                this.type = spawnerBlock.type
                this.totalCount = spawnerBlock.totalCount
                this.location = spawnerBlock.location
            }
        }

        fun save(spawnerBlock: SpawnerBlock): SpawnerBlockEntity? {
            val entity = find(spawnerBlock.location) ?: create(spawnerBlock)

            if (entity.totalCount <= 0) {
                entity.delete()
                return null
            }

            entity.totalCount = spawnerBlock.totalCount

            return entity
        }
    }
}

object SpawnerBlockDao {

    fun find(location: Location): CompletableFuture<SpawnerBlock?> {
        return CompletableFuture.supplyAsync {
            transaction {
                val spawnerBlockEntity = SpawnerBlockEntity.find(location) ?: return@transaction null
                return@transaction spawnerBlockEntity.toSpawnerBlock()
            }
        }.exceptionally {
            it.printStackTrace()
            null
        }
    }

    fun loadAll(): CompletableFuture<List<SpawnerBlock>> {
        return CompletableFuture.supplyAsync {
            transaction {
                SpawnerBlockEntity.all().mapNotNull {
                    it.toSpawnerBlock()
                }
            }
        }.exceptionally {
            it.printStackTrace()
            null
        }
    }

    fun save(spawnerBlock: SpawnerBlock): CompletableFuture<SpawnerBlockEntity?> {
        return CompletableFuture.supplyAsync {
            transaction {
                SpawnerBlockEntity.save(spawnerBlock)
            }
        }.exceptionally {
            it.printStackTrace()
            null
        }
    }

    fun save(spawnerBlocks: Collection<SpawnerBlock>): CompletableFuture<Unit> {
        return CompletableFuture.supplyAsync {
            transaction {
                spawnerBlocks.forEach {
                    SpawnerBlockEntity.save(it)
                }
            }
        }.exceptionally {
            it.printStackTrace()
        }
    }
}