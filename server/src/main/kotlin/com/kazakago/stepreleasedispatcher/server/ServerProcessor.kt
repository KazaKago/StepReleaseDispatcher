package com.kazakago.stepreleasedispatcher.server

import com.google.api.services.androidpublisher.model.Track
import com.google.api.services.androidpublisher.model.TrackRelease
import com.kazakago.stepreleasedispatcher.config.ConfigLoader
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
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.webjars.Webjars
import kotlinx.html.*
import java.util.concurrent.TimeUnit

object ServerProcessor {

    private val nativeServer: ApplicationEngine
    private val config = ConfigLoader.load()
    private val releaseDispatcher: ReleaseDispatcher = initializeReleaseDispatcher()
    private val stepReleaseJobScheduler: StepReleaseJobScheduler<StepReleaseJob> = initializeStepReleaseScheduler()
    private val notificationProviders: NotificationProviders = initializeNotificationProvider()

    init {
        nativeServer = embeddedServer(Netty, port = 8080) {
            install(Webjars)
            routing {
                get("/") {
                    val validConfig = runCatching { releaseDispatcher.validConfig() }
                    val currentTrackInfo = runCatching { releaseDispatcher.currentTrackInfo() }
                    call.respondHtml {
                        head {
                            meta(charset = "utf-8")
                            meta(name = "viewport", content = "width=device-width, initial-scale=1, shrink-to-fit=no")
                            styleLink("/webjars/bootstrap/css/bootstrap.min.css")
                            title { +config.applicationName }
                        }
                        body {
                            nav(classes = "navbar navbar-dark bg-dark") {
                                a(classes = "navbar-brand", href = "#") {
                                    +config.applicationName
                                }
                            }
                            main(classes = "container") {
                                div(classes = "jumbotron jumbotron-fluid") {
                                    div(classes = "container") {
                                        h1(classes = "display-4") {
                                            +"Status"
                                        }
                                        hr(classes = "my-4")
                                        p(classes = "lead") {
                                            if (validConfig.isSuccess) {
                                                +"Config: OK"
                                            } else {
                                                +"Config: Error ${validConfig.exceptionOrNull()?.localizedMessage}"
                                            }
                                        }
                                        p(classes = "lead") {
                                            if (currentTrackInfo.isSuccess) {
                                                +trackInfoToString(currentTrackInfo.getOrThrow())
                                            } else {
                                                +"Error ${currentTrackInfo.exceptionOrNull()?.localizedMessage}"
                                            }
                                        }
                                    }
                                }
                                h2(classes = "display-4") {
                                    +"Operation"
                                }
                                hr(classes = "my-4")
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
                                        textInput(name = "dispatch_schedule", classes = "form-control") { value = config.dispatchSchedule }
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
                                        textInput(name = "package_name", classes = "form-control") { value = config.packageName }
                                    }
                                    p {
                                        submitInput { value = "register" }
                                    }
                                }
                                form("/config/email", encType = FormEncType.multipartFormData, method = FormMethod.post) {
                                    p {
                                        +"ServiceAccountEmail: "
                                        textInput(name = "email", classes = "form-control") { value = config.serviceAccountEmail.toString() }
                                    }
                                    p {
                                        submitInput { value = "register" }
                                    }
                                }
                                form("/config/user_fraction_step", encType = FormEncType.multipartFormData, method = FormMethod.post) {
                                    p {
                                        +"UserFractionStep: "
                                        textInput(name = "user_fraction_step", classes = "form-control") { value = config.userFractionStep.joinToString(",") }
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
                                        textInput(name = "application_name", classes = "form-control") { value = config.applicationName }
                                    }
                                    p {
                                        submitInput { value = "register" }
                                    }
                                }
                                form("/config/slack_web_hook_url", encType = FormEncType.multipartFormData, method = FormMethod.post) {
                                    p {
                                        +"SlackWebHookUrl: "
                                        textInput(name = "slack_web_hook_url", classes = "form-control") { value = config.slackWebHookUrl }
                                    }
                                    p {
                                        submitInput { value = "register" }
                                    }
                                }
                            }
                            script(src = "/webjars/jquery/jquery.min.js") {}
                            script(src = "/webjars/popper.js/umd/popper.min.js") {}
                            script(src = "/webjars/bootstrap/js/bootstrap.min.js") {}
                        }

                    }
                }
                route("/scheduler") {
                    post("/start") {
                        stepReleaseJobScheduler.start()
                        call.respondRedirect("/")
                    }
                    post("/stop") {
                        stepReleaseJobScheduler.shutdown()
                        call.respondRedirect("/")
                    }
                }
                route("/execute") {
                    post("/step_release") {
                        val result = runCatching { dispatchStepRelease() }
                        call.respondHtml {
                            head {
                                title { +config.applicationName }
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
                }
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
                route("/config") {
                    post("/package_name") {
                        call.receiveMultipart().apply {
                            forEachPart { part ->
                                if (part.name == "package_name" && part is PartData.FormItem) {
                                    config.packageName = part.value
                                    ConfigLoader.apply(config)
                                    releaseDispatcher.packageName = part.value
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
                                    config.serviceAccountEmail = part.value
                                    ConfigLoader.apply(config)
                                    releaseDispatcher.serviceAccountEmail = part.value
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
                                    config.applicationName = part.value
                                    ConfigLoader.apply(config)
                                    releaseDispatcher.applicationName = part.value
                                    notificationProviders.applicationName = part.value
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
                                    config.slackWebHookUrl = part.value
                                    ConfigLoader.apply(config)
                                    notificationProviders.slackType = NotificationProvider.Type.Slack(part.value)
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
                                    config.userFractionStep = steps
                                    ConfigLoader.apply(config)
                                    releaseDispatcher.userFractionSteps = steps
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
                                    config.dispatchSchedule = part.value
                                    ConfigLoader.apply(config)
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
                applicationName = config.applicationName,
                slackType = NotificationProvider.Type.Slack(config.slackWebHookUrl))
    }

    private fun initializeStepReleaseScheduler(): StepReleaseJobScheduler<StepReleaseJob> {
        return StepReleaseJobScheduler(
                dispatchSchedule = config.dispatchSchedule,
                jobClass = StepReleaseJob::class)
    }

    private fun initializeReleaseDispatcher(): ReleaseDispatcher {
        return ReleaseDispatcher(
                applicationName = config.applicationName,
                packageName = config.packageName,
                p12File = ConfigLoader.getP12KeyFile(),
                serviceAccountEmail = config.serviceAccountEmail,
                userFractionSteps = config.userFractionStep)
    }

    private fun dispatchStepRelease(): ReleaseDispatcher.StepReleaseResult {
        val result = releaseDispatcher.executeStepRelease()
        when (result) {
            is ReleaseDispatcher.StepReleaseResult.UpdatedTrack -> {
                notificationProviders.postExpansionMessage(result.newTrack, result.oldTrack)
            }
            is ReleaseDispatcher.StepReleaseResult.NoUpdatedTrack -> {
                //do nothing.
            }
        }
        return result
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