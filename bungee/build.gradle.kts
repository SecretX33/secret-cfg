dependencies {
    implementation(project(":core"))
}

tasks.shadowJar {
    archiveFileName.set(rootProject.name + "-bungee.jar")
}
