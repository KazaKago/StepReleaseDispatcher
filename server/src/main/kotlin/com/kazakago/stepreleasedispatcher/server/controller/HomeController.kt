package com.kazakago.stepreleasedispatcher.server.controller

import com.kazakago.stepreleasedispatcher.server.extension.toFormattedString
import com.kazakago.stepreleasedispatcher.server.holder.ConfigHolder
import com.kazakago.stepreleasedispatcher.server.holder.ReleaseDispatcherHolder
import com.kazakago.stepreleasedispatcher.server.holder.StepReleaseSchedulerHolder
import io.ktor.application.call
import io.ktor.html.respondHtml
import io.ktor.routing.Routing
import io.ktor.routing.get
import kotlinx.html.*

fun Routing.home() {
    get("/") {
        val validConfig = runCatching { ReleaseDispatcherHolder.INSTANCE.validConfig() }
        val currentTrackInfo = runCatching { ReleaseDispatcherHolder.INSTANCE.currentTrackInfo() }
        call.respondHtml {
            head {
                meta(charset = "utf-8")
                meta(name = "viewport", content = "width=device-width, initial-scale=1, shrink-to-fit=no")
                styleLink("/webjars/bootstrap/css/bootstrap.min.css")
                title { +ConfigHolder.INSTANCE.applicationName }
            }
            body {
                nav(classes = "navbar navbar-dark bg-dark") {
                    a(classes = "navbar-brand", href = "#") {
                        +ConfigHolder.INSTANCE.applicationName
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
                                    +currentTrackInfo.getOrThrow().toFormattedString()
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
                        if (StepReleaseSchedulerHolder.INSTANCE.isActive()) {
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
                            textInput(name = "dispatch_schedule", classes = "form-control") { value = ConfigHolder.INSTANCE.dispatchSchedule }
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
                            textInput(name = "package_name", classes = "form-control") { value = ConfigHolder.INSTANCE.packageName }
                        }
                        p {
                            submitInput { value = "register" }
                        }
                    }
                    form("/config/email", encType = FormEncType.multipartFormData, method = FormMethod.post) {
                        p {
                            +"ServiceAccountEmail: "
                            textInput(name = "email", classes = "form-control") { value = ConfigHolder.INSTANCE.serviceAccountEmail.toString() }
                        }
                        p {
                            submitInput { value = "register" }
                        }
                    }
                    form("/config/user_fraction_step", encType = FormEncType.multipartFormData, method = FormMethod.post) {
                        p {
                            +"UserFractionStep: "
                            textInput(name = "user_fraction_step", classes = "form-control") { value = ConfigHolder.INSTANCE.userFractionStep.joinToString(",") }
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
                            textInput(name = "application_name", classes = "form-control") { value = ConfigHolder.INSTANCE.applicationName }
                        }
                        p {
                            submitInput { value = "register" }
                        }
                    }
                    form("/config/slack_web_hook_url", encType = FormEncType.multipartFormData, method = FormMethod.post) {
                        p {
                            +"SlackWebHookUrl: "
                            textInput(name = "slack_web_hook_url", classes = "form-control") { value = ConfigHolder.INSTANCE.slackWebHookUrl }
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
}
