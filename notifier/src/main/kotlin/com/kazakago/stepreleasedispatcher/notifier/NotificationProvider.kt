package com.kazakago.stepreleasedispatcher.notifier

import com.google.api.services.androidpublisher.model.Track

interface NotificationProvider {

    class Builder(private val applicationName: String, private val type: Type) {
        fun build(): NotificationProvider {
            return when (type) {
                is Type.Slack -> SlackProvider(applicationName, type.webHookUrl)
            }
        }
    }

    sealed class Type {
        class Slack(val webHookUrl: String) : Type() {
            override fun isValid(): Boolean {
                return webHookUrl.isNotBlank()
            }
        }

        abstract fun isValid(): Boolean
    }

    fun postExpansionMessage(newTrack: Track, oldTrack: Track)

    fun postNoExpansionMessage(currentTrack: Track)

    fun postErrorMessage(exception: Exception)

}
