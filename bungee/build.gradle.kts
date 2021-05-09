import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

dependencies {
    implementation(project(":core"))
}

tasks.shadowJar {
    archiveFileName.set(rootProject.name + "-bungee.jar")
}
