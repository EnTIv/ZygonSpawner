package com.entiv.zygonspawner.spawner

import com.entiv.core.common.debug.debug
import com.entiv.core.common.debug.warn
import com.entiv.zygonspawner.data.SpawnerData
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSuperclassOf

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
            val entityTypes = EntityType.entries
            val entityWeights = mutableMapOf<EntityType, Double>()

            stringList.forEach {
                val split = it.split(":")

                try {
                    @Suppress("UNCHECKED_CAST")
                    val clazz = Class.forName("org.bukkit.entity.${split[0]}").kotlin as KClass<out Entity>
                    val weight = split[1].toDouble()

                    for (entityType in entityTypes) {
                        val entityKClass = entityType.entityClass?.kotlin ?: continue

                        if (clazz.isSubclassOf(LivingEntity::class) && clazz.isSuperclassOf(entityKClass)) {
                            entityWeights.merge(entityType, weight, Double::plus)

                            debug("刷怪笼 $spawnerType 生物 $entityType 的权重为 $weight")
                        }
                    }
                } catch (e: ClassNotFoundException) {
                    // 处理类找不到的异常
                    warn(
                        "无法找到类 ${split[0]}，跳过此实体类型，你可以使用的类如下：AbstractHorse, AbstractSkeleton, AbstractVillager, Ageable, Allay, Ambient, Animals, ArmorStand, Axolotl, Bat, Bee, Blaze, Breedable, Breeze, Camel, Cat, CaveSpider, ChestedHorse, Chicken, Cod, ComplexLivingEntity, Cow, Creature, Creeper, Dolphin, Donkey, Drowned, ElderGuardian, EnderDragon, Enderman, Endermite, Enemy, Evoker, Fish, Flying, Fox, Frog, Ghast, Giant, GlowSquid, Goat, Golem, Guardian, Hoglin, Horse, HumanEntity, Husk, Illager, Illusioner, IronGolem, Llama, MagmaCube, Mob, Monster, Mule, MushroomCow, NPC, Ocelot, Panda, Parrot, Phantom, Pig, Piglin, PiglinAbstract, PiglinBrute, PigZombie, Pillager, Player, PolarBear, PufferFish, Rabbit, Raider, Ravager, Salmon, Sheep, Shulker, Silverfish, Skeleton, SkeletonHorse, Slime, Sniffer, Snowman, Spellcaster, Spider, Squid, Steerable, Stray, Strider, Tadpole, Tameable, TraderLlama, TropicalFish, Turtle, Vex, Villager, Vindicator, WanderingTrader, Warden, WaterMob, Witch, Wither, WitherSkeleton, Wolf, Zoglin, Zombie, ZombieHorse, ZombieVillager"
                    )
                }
            }
            return entityWeights
        }
    }
}
