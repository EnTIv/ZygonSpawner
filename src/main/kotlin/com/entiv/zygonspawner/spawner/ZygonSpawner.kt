package com.entiv.zygonspawner.spawner

import com.entiv.core.common.message.varTag
import com.entiv.zygonspawner.SpawnerData
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

class ZygonSpawner(
    val id: String,
    val name: String,
    val lore: List<String>,
    val minCount: Int,
    val maxCount: Int,
    val weight: Map<EntityType, Double>
) {
    fun generateData(): SpawnerData {
        val selectedType = selectEntityType()
        val generatedCount = Random.nextInt(minCount, maxCount + 1)
        return SpawnerData(id, selectedType, generatedCount)
    }

    private fun selectEntityType(): EntityType {
        val totalWeight = weight.values.sum()
        val randomValue = Random.nextDouble(0.0, totalWeight)
        var selectedType: EntityType? = null
        var cumulativeWeight = 0.0
        for ((type, typeWeight) in weight) {
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
            val durability = parseDurability(section.getString("durability") ?: error("${section.name} 耐久度配置错误"))
            val entityWeights = parseEntityWeights(section.getStringList("刷怪笼类型"))

            val name = section.getString("name", "普通刷怪笼")!!
            val lore = section.getStringList("lore")

            return ZygonSpawner(section.name, name, lore, durability.first, durability.second, entityWeights)
        }

        private fun parseDurability(durabilityString: String): Pair<Int, Int> {
            return durabilityString.split("~")
                .map { it.toInt() }
                .let { Pair(it[0], it[1]) }
        }

        private fun parseEntityWeights(stringList: List<String>): Map<EntityType, Double> {
            val entityTypes = EntityType.entries
            val entityWeights = mutableMapOf<EntityType, Double>()

            stringList.forEach {
                val split = it.split(":")

                @Suppress("UNCHECKED_CAST")
                val clazz = Class.forName("org.bukkit.entity.${split[0]}").kotlin as KClass<out Entity>
                val weight = split[1].toDouble()

                for (entityType in entityTypes) {
                    val entityKClass = entityType.entityClass?.kotlin ?: continue

                    if (clazz.isSubclassOf(LivingEntity::class) && clazz.isSubclassOf(entityKClass)) {
                        entityWeights.merge(entityType, weight, Double::plus)
                    }
                }
            }
            return entityWeights
        }
    }
}