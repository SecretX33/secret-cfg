import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.0"
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

allprojects {
    group = "com.github.secretx33"
    version = "1.0-SNAPSHOT"

    repositories {
        jcenter()
        mavenCentral()
        maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
        maven { url = uri("https://oss.sonatype.org/content/groups/public/") }
        maven { url = uri("https://repo.codemc.org/repository/maven-public/") }
        maven { url = uri("https://plugins.gradle.org/m2/") }
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://maven.enginehub.org/repo/") }
    }
}

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "com.github.johnrengelman.shadow")

    dependencies {
        testImplementation(kotlin("test-junit5"))
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
        implementation("org.spongepowered:configurate-core:4.0.0")
        implementation("org.spongepowered:configurate-yaml:4.0.0")
        implementation("org.spongepowered:configurate-extra-kotlin:4.0.0")
    }

    tasks.test {
        useJUnitPlatform()
    }

    tasks.withType<JavaCompile> {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
        options.encoding = "UTF-8"
    }

    tasks.withType<KotlinCompile> { kotlinOptions.jvmTarget = "1.8" }
}
