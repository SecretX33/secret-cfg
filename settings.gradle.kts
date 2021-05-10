rootProject.name = "secret-cfg"

include(":bukkit")
include(":core")
include(":bungee")

listOf("bukkit", "core", "bungee").forEach {
    include(it)
    findProject(":$it")?.name = "${rootProject.name}-$it"
}
