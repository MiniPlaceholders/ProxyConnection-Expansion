plugins {
    alias(libs.plugins.runpaper)
}

dependencies {
    compileOnly(libs.papermc)
    compileOnly(libs.miniplaceholders)
    implementation(projects.proxyconnectionExpansionCommon)
}