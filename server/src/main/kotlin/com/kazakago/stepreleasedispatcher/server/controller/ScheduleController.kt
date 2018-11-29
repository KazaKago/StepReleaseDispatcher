package com.kazakago.stepreleasedispatcher.server.controller

import com.kazakago.stepreleasedispatcher.server.holder.StepReleaseSchedulerHolder
import io.ktor.application.call
import io.ktor.response.respondRedirect
import io.ktor.routing.Routing
import io.ktor.routing.post
import io.ktor.routing.route

fun Routing.schedule() {
    route("/scheduler") {
        post("/start") {
            StepReleaseSchedulerHolder.INSTANCE.start()
            call.respondRedirect("/")
        }
        post("/stop") {
            StepReleaseSchedulerHolder.INSTANCE.shutdown()
            call.respondRedirect("/")
        }
    }
}