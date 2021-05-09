repositories {
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
}

dependencies {
    implementation(project(":core"))
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3")
    compileOnly("org.spigotmc:spigot-api:1.16.4-R0.1-SNAPSHOT")
    implementation("com.github.cryptomorin:XSeries:7.9.1.1")
}

tasks.shadowJar {
    archiveFileName.set(rootProject.name + "-bukkit.jar")
}
