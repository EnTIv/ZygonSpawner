import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion

val exposedVersion = "0.40.1"

plugins {
    kotlin("jvm") version "1.9.21"
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.1.0"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.2"
}

group = "com.entiv"
version = "1.0.4"


repositories {
    mavenLocal()
    mavenCentral()

    maven { url = uri("https://papermc.io/repo/repository/maven-public/") }
    maven { url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/") }
    maven { url = uri("https://repo.playeranalytics.net/releases") }
    maven { url = uri("https://repo.codemc.org/repository/maven-public/") }
    maven { url = uri("https://repo.purpurmc.org/snapshots") }
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.entiv:InsekiCore:1.2.3")

    compileOnly("org.jetbrains.exposed:exposed-core:$exposedVersion")
    compileOnly("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    compileOnly("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    compileOnly("org.jetbrains.exposed:exposed-java-time:$exposedVersion")

    compileOnly("me.clip:placeholderapi:2.11.0")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("com.zaxxer:HikariCP:5.0.1")
    compileOnly("org.purpurmc.purpur:purpur-api:1.19.2-R0.1-SNAPSHOT")
    compileOnly("de.tr7zw:item-nbt-api-plugin:2.12.1")
}

tasks.shadowJar {

    project.findProperty("outputPath")?.let {
        destinationDirectory.set(file(it.toString()))
    }

    archiveFileName.set("${project.name}-${project.version}.jar")
    relocate("com.entiv.core", "${rootProject.group}.${rootProject.name.toLowerCase()}.lib.core")
    println("导出路径: ${destinationDirectory.get()}")
    println("")

    exclude("org/intellij/lang/**")
    exclude("org/jetbrains/annotations/**")
    exclude("kotlin/**")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

bukkit {
    main = "com.entiv.${rootProject.name.toLowerCase()}.${project.name}"
    author = "EnTIv"
    version = project.version.toString()
    apiVersion = "1.13"
    libraries = listOf(
        "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${getKotlinPluginVersion()}",
        "org.jetbrains.kotlin:kotlin-reflect:${getKotlinPluginVersion()}",
        "org.jetbrains.exposed:exposed-core:$exposedVersion",
        "org.jetbrains.exposed:exposed-dao:$exposedVersion",
        "org.jetbrains.exposed:exposed-jdbc:$exposedVersion",
        "org.jetbrains.exposed:exposed-java-time:$exposedVersion",
        "com.zaxxer:HikariCP:5.0.1",
        "com.h2database:h2:2.2.224"
    )
    softDepend = listOf(
        "Vault",
        "PlaceholderAPI",
    )
}