import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.0"
    id("maven-publish")
    id("com.github.hierynomus.license") version "0.16.1"
}

allprojects {
    group = "com.github.secretx33"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
        jcenter()
        maven { url = uri("https://oss.sonatype.org/content/groups/public/") }
        maven { url = uri("https://repo.codemc.org/repository/maven-public/") }
        maven { url = uri("https://plugins.gradle.org/m2/") }
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://maven.enginehub.org/repo/") }
        mavenLocal()
    }
}

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "maven-publish")
    apply(plugin = "license")

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

    val kotlinComponent: SoftwareComponent = components["kotlin"]

    tasks {
        val sourcesJar by creating(Jar::class) {
            archiveClassifier.set("sources")
            from(sourceSets.main.get().allSource)
        }

        val javadocJar by creating(Jar::class) {
            dependsOn.add(javadoc)
            archiveClassifier.set("javadoc")
            from(javadoc)
        }

        publishing {
            publications {
                create<MavenPublication>("maven") {
                    from(kotlinComponent)

                    artifact(sourcesJar)
                    artifact(javadocJar)
                }
            }
        }
    }

    license {
        header = rootProject.file("LICENSE")
        strictCheck = true
    }
}
