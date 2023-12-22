package com.entiv.zygonspawner.spawner

import com.entiv.core.common.debug.warn
import com.entiv.zygonspawner.data.SpawnerData
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.EntityType
import kotlin.random.Random

class ZygonSpawner(
    val id: String,
    val name: String,
    val lore: List<String>,

    val minTotalCount: Int,
    val maxTotalCount: Int,

    val minSpawnDelay: Int,
    val maxSpawnDelay: Int,
    val spawnCount: Int,
    val maxNearbyEntities: Int,
    val requiredPlayerRange: Int,
    val spawnRange: Int,

    val entityWeights: Map<EntityType, Double>
) {
    fun generateData(): SpawnerData {
        val selectedType = selectEntityType()
        val generatedCount = Random.nextInt(minTotalCount, maxTotalCount + 1)

        return SpawnerData(
            id,
            selectedType,
            generatedCount,
            minSpawnDelay,
            maxSpawnDelay,
            spawnCount,
            maxNearbyEntities,
            requiredPlayerRange,
            spawnRange
        )
    }

    private fun selectEntityType(): EntityType {
        val totalWeight = entityWeights.values.sum()
        val randomValue = Random.nextDouble(0.0, totalWeight)
        var selectedType: EntityType? = null
        var cumulativeWeight = 0.0
        for ((type, typeWeight) in entityWeights) {
            cumulativeWeight += typeWeight
            if (randomValue <= cumulativeWeight) {
                selectedType = type
                break
            }
        }

        return selectedType ?: throw IllegalStateException("No EntityType selected")
    }

    companion object {
        fun fromSection(section: ConfigurationSection): ZygonSpawner {
            val id = section.name
            val totalCount = parseCount(section.getString("totalCount") ?: error("$id 刷怪次数度配置错误"))
            val entityWeights = parseEntityWeights(id, section.getStringList("type"))

            val name = section.getString("name", "普通刷怪笼")!!
            val lore = section.getStringList("lore")

            val minSpawnDelay = section.getInt("minSpawnDelay", 200)
            val maxSpawnDelay = section.getInt("maxSpawnDelay", 800)
            val spawnCount = section.getInt("spawnCount", 4)
            val maxNearbyEntities = section.getInt("maxNearbyEntities", 6)
            val requiredPlayerRange = section.getInt("requiredPlayerRange", 16)
            val spawnRange = section.getInt("spawnRange", 4)

            return ZygonSpawner(
                id = id,
                name = name,
                lore = lore,
                minTotalCount = totalCount.first,
                maxTotalCount = totalCount.second,
                minSpawnDelay = minSpawnDelay,
                maxSpawnDelay = maxSpawnDelay,
                spawnCount = spawnCount,
                maxNearbyEntities = maxNearbyEntities,
                requiredPlayerRange = requiredPlayerRange,
                spawnRange = spawnRange,
                entityWeights = entityWeights
            )
        }

        private fun parseCount(countString: String): Pair<Int, Int> {
            return countString.split("~")
                .map { it.toInt() }
                .let { Pair(it[0], it[1]) }
        }

        private fun parseEntityWeights(spawnerType: String, stringList: List<String>): Map<EntityType, Double> {
            val entityTypes = EntityType.values()
            val entityWeights = mutableMapOf<EntityType, Double>()

            stringList.forEach {
                val split = it.split(":")
                val entityName = split[0]
                val weight = split[1].toDouble()

                val entityType = EntityType.fromName(entityName)
                if (entityType != null) {
                    entityWeights[entityType] = weight
                } else {
                    // 处理未知的实体类型
                    warn("未知的实体类型: $entityName, 跳过此实体类型")
                }
            }
            return entityWeights
        }
    }
}
