package com.kazakago.stepreleasedispatcher.server.controller

import com.kazakago.stepreleasedispatcher.notifier.toFormattedString
import com.kazakago.stepreleasedispatcher.releasedispatcher.ReleaseDispatcher
import com.kazakago.stepreleasedispatcher.server.holder.ConfigHolder
import com.kazakago.stepreleasedispatcher.server.holder.NotificationProvidersHolder
import com.kazakago.stepreleasedispatcher.server.holder.ReleaseDispatcherHolder
import com.kazakago.stepreleasedispatcher.server.html.addBootstrapMetadata
import com.kazakago.stepreleasedispatcher.server.html.addBootstrapScript
import io.ktor.application.call
import io.ktor.html.respondHtml
import io.ktor.routing.Routing
import io.ktor.routing.post
import io.ktor.routing.route
import kotlinx.html.*

fun Routing.execute() {
    route("/execute") {
        post("/step_release") {
            val result = runCatching { dispatchStepRelease() }
            call.respondHtml {
                head {
                    addBootstrapMetadata()
                    title { +ConfigHolder.INSTANCE.applicationName }
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
                                    +nativeResult.newTrack.toFormattedString()
                                }
                                p {
                                    +nativeResult.oldTrack.toFormattedString()
                                }
                            }
                            is ReleaseDispatcher.StepReleaseResult.NoUpdatedTrack -> {
                                h1 {
                                    +"No Updated"
                                }
                                p {
                                    +nativeResult.currentTrack.toFormattedString()
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
                    addBootstrapScript()
                }
            }
        }
    }
}

private fun dispatchStepRelease(): ReleaseDispatcher.StepReleaseResult {
    val result = ReleaseDispatcherHolder.INSTANCE.executeStepRelease()
    when (result) {
        is ReleaseDispatcher.StepReleaseResult.UpdatedTrack -> {
            NotificationProvidersHolder.INSTANCE.postExpansionMessage(result.newTrack, result.oldTrack)
        }
        is ReleaseDispatcher.StepReleaseResult.NoUpdatedTrack -> {
            //do nothing.
        }
    }
    return result
}
