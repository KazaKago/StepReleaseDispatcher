package com.kazakago.stepreleasedispatcher.server.extension

import com.google.api.services.androidpublisher.model.Track
import com.google.api.services.androidpublisher.model.TrackRelease

fun Track.toFormattedString(): String {
    val releaseInfoStringList = releases.map { releaseInfoToString(it) }
    return releaseInfoStringList.joinToString("\n")
}

private fun releaseInfoToString(release: TrackRelease): String {
    var text = "name : " + release.name
    text += "     version : " + release.versionCodes.joinToString(",")
    text += "     status : " + release.status.toString()
    release.userFraction?.let { text += "     userFraction : " + (it * 100).toString() + "%" }
    return text
}
