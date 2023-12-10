package com.entiv.zygonspawner

import org.bukkit.entity.Entity
import kotlin.reflect.KClass

class EntityWeight(
    val clazz: KClass<out Entity>,
    val weight: Boolean
) {
}
