package com.entiv.zygonspawner.storage

import com.entiv.core.exposed.defaultTableName
import com.entiv.core.exposed.entityType
import com.entiv.core.exposed.location
import com.entiv.core.exposed.transaction
import com.entiv.zygonspawner.block.SpawnerBlock
import com.entiv.zygonspawner.data.SpawnerData
import org.bukkit.Location
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

import org.jetbrains.exposed.dao.id.IntIdTable
import java.util.Collections
import java.util.concurrent.CompletableFuture

object SpawnerBlockTable : IntIdTable() {
    override val tableName = defaultTableName()

    val name = varchar("name", 32)
    val entityType = entityType("entityType")
    val count = integer("count")
    val location = location("location").uniqueIndex()
}

class SpawnerBlockEntity(id: EntityID<Int>) : IntEntity(id) {
    var name by SpawnerBlockTable.name
    var type by SpawnerBlockTable.entityType
    var count by SpawnerBlockTable.count
    var location by SpawnerBlockTable.location

    fun toSpawnerBlock(): SpawnerBlock {
        val spawnerData = SpawnerData(name, type, count)
        return SpawnerBlock(spawnerData, location)
    }

    companion object : IntEntityClass<SpawnerBlockEntity>(SpawnerBlockTable) {
        fun find(location: Location): SpawnerBlockEntity? {
            return find { SpawnerBlockTable.location eq location }.singleOrNull()
        }

        fun create(spawnerBlock: SpawnerBlock): SpawnerBlockEntity {
            return SpawnerBlockEntity.new {
                this.name = spawnerBlock.name
                this.type = spawnerBlock.type
                this.count = spawnerBlock.count
                this.location = spawnerBlock.location
            }
        }

        fun save(spawnerBlock: SpawnerBlock): SpawnerBlockEntity? {
            val entity = find(spawnerBlock.location) ?: create(spawnerBlock)

            if (entity.count <= 0) {
                entity.delete()
                return null
            }

            entity.count = spawnerBlock.count

            return entity
        }
    }
}

object SpawnerBlockDao {

    fun find(location: Location): CompletableFuture<SpawnerBlock?> {
        return CompletableFuture.supplyAsync {
            transaction {
                SpawnerBlockEntity.find(location)?.toSpawnerBlock()
            }
        }
    }

    fun loadAll(): CompletableFuture<Collection<SpawnerBlock>> {
        return CompletableFuture.supplyAsync {
            transaction {
                SpawnerBlockEntity.all().map {
                    if (it.count <= 0) {
                        it.delete()
                    }
                    it.toSpawnerBlock()
                }
            }
        }
    }

    fun save(spawnerBlock: SpawnerBlock): CompletableFuture<SpawnerBlockEntity> {
        return CompletableFuture.supplyAsync {
            transaction {
                SpawnerBlockEntity.save(spawnerBlock)
            }
        }
    }

    fun save(spawnerBlocks: Collection<SpawnerBlock>): CompletableFuture<Unit> {
        return CompletableFuture.supplyAsync {
            transaction {
                spawnerBlocks.forEach {
                    SpawnerBlockEntity.save(it)
                }
            }
        }
    }
}