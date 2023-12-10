package com.entiv.zygonspawner

import com.entiv.core.utils.RandomUtil
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemStack
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

class SpawnerGenerator(
    val id: String,
    val name: Component,
    val lore: List<Component>,
    val durability: Int,
    val entityType: EntityType
) {
    fun generate(): ItemStack {

    }

    companion object {
        fun fromSection(section: ConfigurationSection): SpawnerGenerator {
            val miniMessage = MiniMessage.miniMessage()

            val name = (section.getString("name") ?: "").let {
                miniMessage.deserialize(it)
            }
            val lore = section.getStringList("lore").map {
                miniMessage.deserialize(it)
            }
            val durability = (section.getString("durability") ?: "").let {
                val (min, max) = it.split("~").map { value -> value.toInt() }
                RandomUtil.randomInt(min, max)
            }

            val entityWeights = section.getStringList("刷怪笼类型").let { stringList ->
                val entityTypes = EntityType.entries
                val entityWeights = mutableMapOf<EntityType, Double>()

                stringList.forEach {
                    val split = it.split(":")

                    @Suppress("UNCHECKED_CAST")
                    val clazz = Class.forName("org.bukkit.entity.${split[0]}").kotlin as KClass<out Entity>
                    val weight = split[1].toDouble()

                    entityTypes.forEach { entityType ->
                        val entityKClass = entityType.entityClass?.kotlin ?: return@forEach

                        if (clazz.isSubclassOf(entityKClass)) {
                            entityWeights.merge(entityType, weight, Double::plus)
                        }
                    }
                }

                entityWeights
            }

            return SpawnerGenerator(section.name, name, lore, durability, entityWeights)
        }
    }
}