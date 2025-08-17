plugins {
    id("fabric-loom")
}

dependencies {
    compileOnly(libs.miniplaceholders)
    compileOnly(projects.proxyconnectionExpansionCommon)
    minecraft(libs.minecraft)
    mappings(loom.officialMojangMappings())
    modCompileOnly(libs.fabric.loader)
    modCompileOnly(libs.fabric.api)
    modCompileOnly(libs.adventure.platform.fabric)
//    modCompileOnly("com.pokeskies:fabricpluginmessage:1.0.0")
    modCompileOnly("maven.modrinth:fabric-plugin-messaging:1.0.0")
}