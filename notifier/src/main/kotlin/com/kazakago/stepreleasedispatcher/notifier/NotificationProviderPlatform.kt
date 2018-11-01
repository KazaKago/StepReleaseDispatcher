package com.kazakago.stepreleasedispatcher.notifier

import com.google.api.services.androidpublisher.model.Track
import com.google.api.services.androidpublisher.model.TrackRelease
import java.net.URL

abstract class NotificationProviderPlatform(protected open val applicationName: String) : NotificationProvider {

    protected open fun botUserName(): String {
        return applicationName
    }

    protected open fun botIconUrl(): URL {
        return URL("https://emoji.slack-edge.com/T025D9EDN/android_aya_with/6ae005a3ae4d4104.png")
    }

    protected open fun expansionSummaryMessage(): String {
        return "Scope of user fraction has been expanded!"
    }

    protected open fun oldTrackTitle(): String {
        return "Before Update Track Info"
    }

    protected open fun oldTrackHexColor(): String {
        return "#0091EA"
    }

    protected open fun newTrackTitle(): String {
        return "After Update Track Info"
    }

    protected open fun newTrackHexColor(): String {
        return "#00E676"
    }

    protected open fun noExpansionSummaryMessage(): String {
        return "There was no scope in the expansion."
    }

    protected open fun currentTrackTitle(): String {
        return "Current Track Info"
    }

    protected open fun currentTrackHexColor(): String {
        return "#00E676"
    }

    protected open fun errorSummaryMessage(): String {
        return "An error occurred during processing!"
    }

    protected open fun errorTrackTitle(): String {
        return "Error Info"
    }

    protected open fun errorHexColor(): String {
        return "#F44336"
    }

    protected open fun trackInfoToString(track: Track): String {
        val releaseInfoStringList = track.releases.map { releaseInfoToString(it) }
        return releaseInfoStringList.joinToString("\n")
    }

    protected open fun releaseInfoToString(release: TrackRelease): String {
        var text = "name : " + release.name
        text += "     version : " + release.versionCodes.joinToString(",")
        text += "     status : " + release.status.toString()
        release.userFraction?.let { text += "     userFraction : " + (it * 100).toString() + "%" }
        return text
    }

}
