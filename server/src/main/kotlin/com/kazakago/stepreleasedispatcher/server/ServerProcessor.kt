package com.kazakago.stepreleasedispatcher.server

import com.google.api.services.androidpublisher.model.Track
import com.google.api.services.androidpublisher.model.TrackRelease
import com.kazakago.stepreleasedispatcher.config.ApplicationConfig
import com.kazakago.stepreleasedispatcher.notifier.NotificationProvider
import com.kazakago.stepreleasedispatcher.notifier.NotificationProviders
import com.kazakago.stepreleasedispatcher.releasedispatcher.ReleaseDispatcher
import com.kazakago.stepreleasedispatcher.scheduler.StepReleaseJobScheduler
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.html.respondHtml
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.request.receiveMultipart
import io.ktor.response.respondRedirect
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.webjars.Webjars
import kotlinx.html.*
import java.util.concurrent.TimeUnit

object ServerProcessor {

    private val nativeServer: ApplicationEngine
    private val releaseDispatcher: ReleaseDispatcher = initializeReleaseDispatcher()
    private val stepReleaseJobScheduler: StepReleaseJobScheduler<StepReleaseJob> = initializeStepReleaseScheduler()
    private val notificationProviders: NotificationProviders = initializeNotificationProvider()

    init {
        nativeServer = embeddedServer(Netty, port = 8080) {
            install(Webjars)
            routing {
                get("/") {
                    val validConfig = runCatching { releaseDispatcher.validConfig() }
                    val currentTrackInfo = runCatching { (releaseDispatcher.currentTrackInfo()) }
                    call.respondHtml {
                        head {
                            title { +ApplicationConfig.getApplicationName() }
                            styleLink("/webjars/bootstrap/css/bootstrap.min.css")
                            script(src = "/webjars/jquery/jquery.min.js") {}
                            script(src = "/webjars/popper.js/umd/popper.min.js") {}
                            script(src = "/webjars/bootstrap/js/bootstrap.min.js") {}
                        }
                        body {
                            h1 {
                                +ApplicationConfig.getApplicationName()
                            }
                            h2 {
                                +"Status"
                            }
                            p {
                                if (validConfig.isSuccess) {
                                    +"Config: OK"
                                } else {
                                    +"Config: Error ${validConfig.exceptionOrNull()?.localizedMessage}"
                                }
                            }
                            p {
                                if (currentTrackInfo.isSuccess) {
                                    +trackInfoToString(currentTrackInfo.getOrThrow())
                                } else {
                                    +"Error ${currentTrackInfo.exceptionOrNull()?.localizedMessage}"
                                }
                            }
                            h2 {
                                +"Operation"
                            }
                            form("/execute/step_release", method = FormMethod.post) {
                                p {
                                    submitInput { value = "executeStepRelease" }
                                }
                            }
                            h2 {
                                +"Schedule"
                            }
                            p {
                                if (stepReleaseJobScheduler.isActive()) {
                                    +"Status: Active"
                                } else {
                                    +"Status: Inactive"
                                }
                            }
                            p {
                                +"Scheduler Switch"
                            }
                            p {
                                form("/scheduler/start", method = FormMethod.post) {
                                    submitInput { value = "start" }
                                }
                                form("/scheduler/stop", method = FormMethod.post) {
                                    submitInput { value = "stop" }
                                }
                            }
                            form("/config/dispatch_schedule", encType = FormEncType.multipartFormData, method = FormMethod.post) {
                                p {
                                    +"DispatchSchedule: "
                                    textInput(name = "dispatch_schedule") { value = ApplicationConfig.getDispatchSchedule() }
                                }
                                p {
                                    submitInput { value = "register" }
                                }
                            }
                            h2 {
                                +"Config"
                            }
                            h3 {
                                +"Required"
                            }
                            form("/config/package_name", encType = FormEncType.multipartFormData, method = FormMethod.post) {
                                p {
                                    +"PackageName: "
                                    textInput(name = "package_name") { value = ApplicationConfig.getPackageName() }
                                }
                                p {
                                    submitInput { value = "register" }
                                }
                            }
                            form("/config/email", encType = FormEncType.multipartFormData, method = FormMethod.post) {
                                p {
                                    +"ServiceAccountEmail: "
                                    textInput(name = "email") { value = ApplicationConfig.getServiceAccountEmail() }
                                }
                                p {
                                    submitInput { value = "register" }
                                }
                            }
                            form("/config/user_fraction_step", encType = FormEncType.multipartFormData, method = FormMethod.post) {
                                p {
                                    +"UserFractionStep: "
                                    textInput(name = "user_fraction_step") { value = ApplicationConfig.getUserFractionStep().joinToString(",") }
                                }
                                p {
                                    submitInput { value = "register" }
                                }
                            }
                            form("/upload/p12", encType = FormEncType.multipartFormData, method = FormMethod.post) {
                                acceptCharset = "utf-8"
                                p {
                                    label { +"p12 File field: " }
                                    fileInput { name = "p12file" }
                                }
                                p {
                                    submitInput { value = "upload" }
                                }
                            }
                            h3 {
                                +"Optional"
                            }
                            form("/config/application_name", encType = FormEncType.multipartFormData, method = FormMethod.post) {
                                p {
                                    +"ApplicationName: "
                                    textInput(name = "application_name") { value = ApplicationConfig.getApplicationName() }
                                }
                                p {
                                    submitInput { value = "register" }
                                }
                            }
                            form("/config/slack_web_hook_url", encType = FormEncType.multipartFormData, method = FormMethod.post) {
                                p {
                                    +"SlackWebHookUrl: "
                                    textInput(name = "slack_web_hook_url") { value = ApplicationConfig.getSlackWebHookUrl() }
                                }
                                p {
                                    submitInput { value = "register" }
                                }
                            }
                        }
                    }
                }
                post("/scheduler/start") {
                    stepReleaseJobScheduler.start()
                    call.respondRedirect("/")
                }
                post("/scheduler/stop") {
                    stepReleaseJobScheduler.shutdown()
                    call.respondRedirect("/")
                }
                post("/execute/step_release") {
                    val result = runCatching { dispatchStepRelease() }
                    call.respondHtml {
                        head {
                            title { +ApplicationConfig.getApplicationName() }
                            styleLink("/webjars/bootstrap/css/bootstrap.min.css")
                            script(src = "/webjars/jquery/jquery.min.js") {}
                            script(src = "/webjars/popper.js/umd/popper.min.js") {}
                            script(src = "/webjars/bootstrap/js/bootstrap.min.js") {}
                        }
                        body {
                            if (result.isSuccess) {
                                val nativeResult = result.getOrThrow()
                                when (nativeResult) {
                                    is ReleaseDispatcher.StepReleaseResult.UpdatedTrack -> {
                                        h1 {
                                            +"Track Updated!"
                                        }
                                        p {
                                            +trackInfoToString(nativeResult.newTrack)
                                        }
                                        p {
                                            +trackInfoToString(nativeResult.oldTrack)
                                        }
                                    }
                                    is ReleaseDispatcher.StepReleaseResult.NoUpdatedTrack -> {
                                        h1 {
                                            +"No Updated"
                                        }
                                        p {
                                            +trackInfoToString(nativeResult.currentTrack)
                                        }
                                    }
                                }
                            } else {
                                h1 {
                                    +"Error Occurred"
                                }
                                p {
                                    +"Error ${result.exceptionOrNull()?.localizedMessage}"
                                }
                            }
                            form("/", method = FormMethod.get) {
                                p {
                                    submitInput { value = "back to Top" }
                                }
                            }
                        }
                    }
                }
                post("/upload/p12") {
                    call.receiveMultipart().apply {
                        forEachPart { part ->
                            if (part.name == "p12file" && part is PartData.FileItem) {
                                part.streamProvider().use { inputStream -> ApplicationConfig.putP12KeyFile(inputStream) }
                            }
                            part.dispose()
                        }
                    }
                    call.respondRedirect("/")
                }
                post("/config/package_name") {
                    call.receiveMultipart().apply {
                        forEachPart { part ->
                            if (part.name == "package_name" && part is PartData.FormItem) {
                                ApplicationConfig.putPackageName(part.value)
                                releaseDispatcher.packageName = part.value
                            }
                            part.dispose()
                        }
                    }
                    call.respondRedirect("/")
                }
                post("/config/email") {
                    call.receiveMultipart().apply {
                        forEachPart { part ->
                            if (part.name == "email" && part is PartData.FormItem) {
                                ApplicationConfig.putServiceAccountEmail(part.value)
                                releaseDispatcher.serviceAccountEmail = part.value
                            }
                            part.dispose()
                        }
                    }
                    call.respondRedirect("/")
                }
                post("/config/application_name") {
                    call.receiveMultipart().apply {
                        forEachPart { part ->
                            if (part.name == "application_name" && part is PartData.FormItem) {
                                ApplicationConfig.putApplicationName(part.value)
                                releaseDispatcher.applicationName = part.value
                                notificationProviders.applicationName = part.value
                            }
                            part.dispose()
                        }
                    }
                    call.respondRedirect("/")
                }
                post("/config/slack_web_hook_url") {
                    call.receiveMultipart().apply {
                        forEachPart { part ->
                            if (part.name == "slack_web_hook_url" && part is PartData.FormItem) {
                                ApplicationConfig.putSlackWebHookUrl(part.value)
                                notificationProviders.slackType = NotificationProvider.Type.Slack(part.value)
                            }
                            part.dispose()
                        }
                    }
                    call.respondRedirect("/")
                }
                post("/config/user_fraction_step") {
                    call.receiveMultipart().apply {
                        forEachPart { part ->
                            if (part.name == "user_fraction_step" && part is PartData.FormItem) {
                                val steps = part.value.split(",").map { it.toDouble() }
                                ApplicationConfig.putUserFractionStep(steps)
                                releaseDispatcher.userFractionSteps = steps
                            }
                            part.dispose()
                        }
                    }
                    call.respondRedirect("/")
                }
                post("/config/dispatch_schedule") {
                    call.receiveMultipart().apply {
                        forEachPart { part ->
                            if (part.name == "dispatch_schedule" && part is PartData.FormItem) {
                                ApplicationConfig.putDispatchSchedule(part.value)
                                stepReleaseJobScheduler.dispatchSchedule = part.value
                            }
                            part.dispose()
                        }
                    }
                    call.respondRedirect("/")
                }
            }
        }
    }

    fun start() {
        nativeServer.start(wait = true)
    }

    fun stop() {
        stepReleaseJobScheduler.shutdown()
        nativeServer.stop(1000, 30, TimeUnit.SECONDS)
    }

    private fun initializeNotificationProvider(): NotificationProviders {
        return NotificationProviders(
                applicationName = ApplicationConfig.getApplicationName(),
                slackType = NotificationProvider.Type.Slack(ApplicationConfig.getSlackWebHookUrl()))
    }

    private fun initializeStepReleaseScheduler(): StepReleaseJobScheduler<StepReleaseJob> {
        return StepReleaseJobScheduler(
                dispatchSchedule = ApplicationConfig.getDispatchSchedule(),
                jobClass = StepReleaseJob::class)
    }

    private fun initializeReleaseDispatcher(): ReleaseDispatcher {
        return ReleaseDispatcher(
                applicationName = ApplicationConfig.getApplicationName(),
                packageName = ApplicationConfig.getPackageName(),
                p12File = ApplicationConfig.getP12KeyFile(),
                serviceAccountEmail = ApplicationConfig.getServiceAccountEmail(),
                userFractionSteps = ApplicationConfig.getUserFractionStep())
    }

    private fun dispatchStepRelease(): ReleaseDispatcher.StepReleaseResult {
        return releaseDispatcher.executeStepRelease()
    }

    private fun trackInfoToString(track: Track): String {
        val releaseInfoStringList = track.releases.map { releaseInfoToString(it) }
        return releaseInfoStringList.joinToString("\n")
    }

    private fun releaseInfoToString(release: TrackRelease): String {
        var text = "name : " + release.name
        text += "     version : " + release.versionCodes.joinToString(",")
        text += "     status : " + release.status.toString()
        release.userFraction?.let { text += "     userFraction : " + (it * 100).toString() + "%" }
        return text
    }

}