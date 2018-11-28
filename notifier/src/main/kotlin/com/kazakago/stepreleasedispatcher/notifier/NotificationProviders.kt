package com.kazakago.stepreleasedispatcher.notifier

import com.google.api.services.androidpublisher.model.Track

class NotificationProviders(var applicationName: String, slackType: NotificationProvider.Type.Slack? = null) {

    var slackType = slackType
        set(value) {
            field = value
            notificationProviderList = initializeProviderList()
        }
    private var notificationProviderList = listOf<NotificationProvider>()

    private fun initializeProviderList(): List<NotificationProvider> {
        val providerList = mutableListOf<NotificationProvider>()
        slackType?.let { if (it.isValid()) providerList.add(NotificationProvider.Builder(applicationName, it).build()) }
        return providerList
    }

    fun postExpansionMessage(newTrack: Track, oldTrack: Track) {
        notificationProviderList.forEach { it.postExpansionMessage(newTrack, oldTrack) }
    }

    fun postNoExpansionMessage(currentTrack: Track) {
        notificationProviderList.forEach { it.postNoExpansionMessage(currentTrack) }
    }

    fun postErrorMessage(exception: Exception) {
        notificationProviderList.forEach { it.postErrorMessage(exception) }
    }

}
