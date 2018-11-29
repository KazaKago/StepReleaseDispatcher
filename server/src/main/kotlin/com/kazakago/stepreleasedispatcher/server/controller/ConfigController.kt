package com.kazakago.stepreleasedispatcher.server.controller

import com.kazakago.stepreleasedispatcher.notifier.NotificationProvider
import com.kazakago.stepreleasedispatcher.server.holder.ConfigHolder
import com.kazakago.stepreleasedispatcher.server.holder.NotificationProvidersHolder
import com.kazakago.stepreleasedispatcher.server.holder.ReleaseDispatcherHolder
import com.kazakago.stepreleasedispatcher.server.holder.StepReleaseSchedulerHolder
import io.ktor.application.call
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.request.receiveMultipart
import io.ktor.response.respondRedirect
import io.ktor.routing.Routing
import io.ktor.routing.post
import io.ktor.routing.route

fun Routing.config() {
    route("/config") {
        post("/package_name") {
            call.receiveMultipart().apply {
                forEachPart { part ->
                    if (part.name == "package_name" && part is PartData.FormItem) {
                        ConfigHolder.INSTANCE.packageName = part.value
                        ConfigHolder.apply()
                        ReleaseDispatcherHolder.INSTANCE.packageName = part.value
                    }
                    part.dispose()
                }
            }
            call.respondRedirect("/")
        }
        post("/email") {
            call.receiveMultipart().apply {
                forEachPart { part ->
                    if (part.name == "email" && part is PartData.FormItem) {
                        ConfigHolder.INSTANCE.serviceAccountEmail = part.value
                        ConfigHolder.apply()
                        ReleaseDispatcherHolder.INSTANCE.serviceAccountEmail = part.value
                    }
                    part.dispose()
                }
            }
            call.respondRedirect("/")
        }
        post("/application_name") {
            call.receiveMultipart().apply {
                forEachPart { part ->
                    if (part.name == "application_name" && part is PartData.FormItem) {
                        ConfigHolder.INSTANCE.applicationName = part.value
                        ConfigHolder.apply()
                        ReleaseDispatcherHolder.INSTANCE.applicationName = part.value
                        NotificationProvidersHolder.INSTANCE.applicationName = part.value
                    }
                    part.dispose()
                }
            }
            call.respondRedirect("/")
        }
        post("/slack_web_hook_url") {
            call.receiveMultipart().apply {
                forEachPart { part ->
                    if (part.name == "slack_web_hook_url" && part is PartData.FormItem) {
                        ConfigHolder.INSTANCE.slackWebHookUrl = part.value
                        ConfigHolder.apply()
                        NotificationProvidersHolder.INSTANCE.slackType = NotificationProvider.Type.Slack(part.value)
                    }
                    part.dispose()
                }
            }
            call.respondRedirect("/")
        }
        post("/user_fraction_step") {
            call.receiveMultipart().apply {
                forEachPart { part ->
                    if (part.name == "user_fraction_step" && part is PartData.FormItem) {
                        val steps = part.value.trim().split(",").map { it.toDouble() }
                        ConfigHolder.INSTANCE.userFractionStep = steps
                        ConfigHolder.apply()
                        ReleaseDispatcherHolder.INSTANCE.userFractionSteps = steps
                    }
                    part.dispose()
                }
            }
            call.respondRedirect("/")
        }
        post("/dispatch_schedule") {
            call.receiveMultipart().apply {
                forEachPart { part ->
                    if (part.name == "dispatch_schedule" && part is PartData.FormItem) {
                        ConfigHolder.INSTANCE.dispatchSchedule = part.value
                        ConfigHolder.apply()
                        StepReleaseSchedulerHolder.INSTANCE.dispatchSchedule = part.value
                    }
                    part.dispose()
                }
            }
            call.respondRedirect("/")
        }
    }
}