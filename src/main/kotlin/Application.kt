package com.techbros

import com.techbros.plugins.configureCORS
import com.techbros.plugins.configureDatabases
import com.techbros.plugins.configureMonitoring
import com.techbros.plugins.configureRouting
import com.techbros.plugins.configureSerialization
import com.techbros.plugins.configureStatusPages
import io.ktor.server.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureDatabases()
    configureSerialization()
    configureMonitoring()
    configureStatusPages()
    configureCORS()
    configureRouting()
}