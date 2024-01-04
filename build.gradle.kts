import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion

plugins {
    kotlin("jvm") version "1.9.21"
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.1.0"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.2"
}

group = "com.entiv"
version = "1.0.2"

repositories {
    mavenLocal()
    mavenCentral()

    maven { url = uri("https://papermc.io/repo/repository/maven-public/") }
    maven { url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/") }
    maven { url = uri("https://repo.playeranalytics.net/releases") }
    maven { url = uri("https://repo.codemc.org/repository/maven-public/") }
    maven { url = uri("https://repo.purpurmc.org/snapshots") }
    maven { url = uri("https://repo.xenondevs.xyz/releases") }

}

val exposedVersion = "0.40.1"
val coreVersion = "2.0.1"

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.entiv.InsekiCore:module-common:$coreVersion")
    implementation("com.entiv.InsekiCore:module-command:$coreVersion")
    implementation("com.entiv.InsekiCore:module-exposed:$coreVersion")
    implementation("com.entiv.InsekiCore:module-menu:$coreVersion")
    implementation("xyz.xenondevs.invui:invui:1.14")
    implementation("xyz.xenondevs.invui:invui-kotlin:1.14") {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.jetbrains", module = "annotations")
    }
    compileOnly("org.jetbrains.kotlin:kotlin-reflect:${getKotlinPluginVersion()}")
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${getKotlinPluginVersion()}")
    compileOnly("io.papermc.paper:paper-api:1.18.2-R0.1-SNAPSHOT")
    compileOnly("de.tr7zw:item-nbt-api-plugin:2.12.1")

    compileOnly("org.jetbrains.exposed:exposed-core:$exposedVersion")
    compileOnly("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    compileOnly("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    compileOnly("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
}

tasks.shadowJar {
    project.findProperty("outputPath")?.let {
        val outputPath = it.toString()
        destinationDirectory.set(file(outputPath))
    }

    archiveFileName.set("${project.name}-${project.version}.jar")
    relocate("com.entiv.core", "${project.group}.${project.name.toLowerCase()}.lib.core")
    println("导出路径: ${destinationDirectory.get()}")

    exclude("org/intellij/lang/annotations/**")
    exclude("org/jetbrains/annotations/")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

bukkit {
    main = "com.entiv.${project.name.toLowerCase()}.${project.name}Plugin"
    author = "EnTIv"
    version = project.version.toString()
    apiVersion = "1.13"
    libraries = listOf(
        "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${getKotlinPluginVersion()}",
        "org.jetbrains.kotlin:kotlin-reflect:${getKotlinPluginVersion()}",
        "com.zaxxer:HikariCP:5.0.1",
        "org.jetbrains.exposed:exposed-core:$exposedVersion",
        "org.jetbrains.exposed:exposed-dao:$exposedVersion",
        "org.jetbrains.exposed:exposed-jdbc:$exposedVersion",
        "org.jetbrains.exposed:exposed-java-time:$exposedVersion",
        "com.h2database:h2:2.2.224",
    )
    softDepend = listOf(
        "Vault",
        "PlaceholderAPI",
    )
}
