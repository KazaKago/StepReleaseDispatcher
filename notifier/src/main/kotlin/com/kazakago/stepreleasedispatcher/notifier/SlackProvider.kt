package com.kazakago.stepreleasedispatcher.notifier

import com.google.api.services.androidpublisher.model.Track
import net.gpedro.integrations.slack.SlackApi
import net.gpedro.integrations.slack.SlackAttachment
import net.gpedro.integrations.slack.SlackMessage

class SlackProvider(applicationName: String, slackWebHookUrl: String) : NotificationProviderPlatform(applicationName) {

    private val slackApi = SlackApi(slackWebHookUrl)

    override fun postExpansionMessage(newTrack: Track, oldTrack: Track) {
        slackApi.call(SlackMessage(botUserName(), expansionSummaryMessage())
                .setIcon(botIconUrl().toString())
                .addAttachments(SlackAttachment("")
                        .setColor(oldTrackHexColor())
                        .setTitle(oldTrackTitle())
                        .setText(trackInfoToString(oldTrack)))
                .addAttachments(SlackAttachment("")
                        .setColor(newTrackHexColor())
                        .setTitle(newTrackTitle())
                        .setText(trackInfoToString(newTrack))))
    }

    override fun postNoExpansionMessage(currentTrack: Track) {
        slackApi.call(SlackMessage(botUserName(), noExpansionSummaryMessage())
                .setIcon(botIconUrl().toString())
                .addAttachments(SlackAttachment("")
                        .setColor(currentTrackHexColor())
                        .setTitle(currentTrackTitle())
                        .setText(trackInfoToString(currentTrack))))
    }

    override fun postErrorMessage(exception: Exception) {
        slackApi.call(SlackMessage(botUserName(), errorSummaryMessage())
                .setIcon(botIconUrl().toString())
                .addAttachments(SlackAttachment("")
                        .setColor(errorHexColor())
                        .setTitle(errorTrackTitle())
                        .setText(exception.localizedMessage)))
    }

}