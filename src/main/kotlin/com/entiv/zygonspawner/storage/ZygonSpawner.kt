package com.entiv.zygonspawner.storage

import com.entiv.core.common.kit.defaultTableName
import org.bukkit.Bukkit
import org.bukkit.Location
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import java.sql.PreparedStatement
import org.jetbrains.exposed.sql.VarCharColumnType

object ZygonSpawnerTable : IntIdTable() {
    override val tableName = defaultTableName()

    val name = varchar("name", 32)
    val location = location("location")
}
