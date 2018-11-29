package com.kazakago.stepreleasedispatcher.server.controller

import com.kazakago.stepreleasedispatcher.config.ConfigLoader
import io.ktor.application.call
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.request.receiveMultipart
import io.ktor.response.respondRedirect
import io.ktor.routing.Routing
import io.ktor.routing.post
import io.ktor.routing.route

fun Routing.upload() {
    route("/upload") {
        post("/p12") {
            call.receiveMultipart().apply {
                forEachPart { part ->
                    if (part.name == "p12file" && part is PartData.FileItem) {
                        part.streamProvider().use { inputStream -> ConfigLoader.putP12KeyFile(inputStream) }
                    }
                    part.dispose()
                }
            }
            call.respondRedirect("/")
        }
    }
}