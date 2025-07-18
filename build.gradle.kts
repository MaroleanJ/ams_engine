
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
}

group = "com.techbros"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

repositories {
    mavenCentral()
}

tasks.register("printJarPath") {
    dependsOn("shadowJar") // <-- this ensures shadowJar runs first
    doLast {
        println("========== Listing build/libs ==========")
        file("build/libs").listFiles()?.forEach {
            println("📦 " + it.name)
        } ?: println("No files found.")
    }
}


tasks {
    shadowJar {
        archiveBaseName.set("ams-engine")
        archiveClassifier.set("")     // <- remove the "all"
        archiveVersion.set("")        // <- no version suffix
    }
    build {
        dependsOn(shadowJar) // ensures shadowJar runs before build
    }
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.postgresql)
    implementation(libs.h2)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.host.common)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.config.yaml)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)

    // For password hashing
    implementation("org.mindrot:jbcrypt:0.4")
    implementation("org.jetbrains.exposed:exposed-java-time:0.61.0")

    implementation("org.postgresql:postgresql:42.7.1") // or latest version

}
