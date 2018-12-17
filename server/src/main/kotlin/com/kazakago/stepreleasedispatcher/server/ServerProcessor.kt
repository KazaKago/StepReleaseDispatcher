package com.kazakago.stepreleasedispatcher.server

import com.kazakago.stepreleasedispatcher.server.controller.*
import com.kazakago.stepreleasedispatcher.server.holder.StepReleaseSchedulerHolder
import io.ktor.application.install
import io.ktor.routing.routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.webjars.Webjars
import java.util.concurrent.TimeUnit

object ServerProcessor {

    private val nativeServer: ApplicationEngine

    init {
        nativeServer = embeddedServer(Netty, port = 4010) {
            install(Webjars)
            routing {
                home()
                schedule()
                execute()
                upload()
                config()
            }
        }
    }

    fun start() {
        nativeServer.start(wait = true)
    }

    fun stop() {
        StepReleaseSchedulerHolder.INSTANCE.shutdown()
        nativeServer.stop(1000, 30, TimeUnit.SECONDS)
    }

}