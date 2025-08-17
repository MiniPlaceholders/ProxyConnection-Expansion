plugins {
    java
    alias(libs.plugins.idea.ext)
    alias(libs.plugins.blossom)
    alias(libs.plugins.shadow)
}

dependencies {
    compileOnly(libs.miniplaceholders)
    compileOnly(libs.adventure.api)
    compileOnly(libs.adventure.minimessage)
    implementation(projects.proxyconnectionExpansionPaper)
    implementation(projects.proxyconnectionExpansionFabric)
    implementation(projects.proxyconnectionExpansionSponge)
    implementation(projects.proxyconnectionExpansionCommon)
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))
tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(21)
    }
}

subprojects {
    apply<JavaPlugin>()
    java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    tasks {
        compileJava {
            options.encoding = Charsets.UTF_8.name()
            options.release.set(21)
        }
    }
}

tasks {
    shadowJar {
        archiveFileName.set("ProxyConnection-Expansion-${project.version}.jar")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
    build {
        dependsOn(shadowJar)
    }
}

sourceSets {
    main {
        blossom {
            javaSources {
                property("version", project.version.toString())
            }
        }
    }
}