package com.kazakago.stepreleasedispatcher.releasedispatcher

import com.google.api.client.repackaged.com.google.common.base.Preconditions
import com.google.api.services.androidpublisher.model.Track
import java.io.File

class ReleaseDispatcher(var applicationName: String, var packageName: String, var p12File: File, var serviceAccountEmail: String, var userFractionSteps: List<Double>) {

    private sealed class ReleaseUpdateType {
        object None : ReleaseUpdateType()
        class Expand(val versionCodes: List<Long>, val userFraction: Double) : ReleaseUpdateType()
        class Complete(val versionCodes: List<Long>) : ReleaseUpdateType()
    }

    sealed class StepReleaseResult {
        class UpdatedTrack(val newTrack: Track, val oldTrack: Track) : StepReleaseResult()
        class NoUpdatedTrack(val currentTrack: Track) : StepReleaseResult()
    }

    fun validConfig() {
        Preconditions.checkArgument(packageName.isNotEmpty(), "ApplicationConfig.PACKAGE_NAME cannot be null or empty!")
        Preconditions.checkArgument(p12File.exists(), "ApplicationConfig.SRC_RESOURCES_KEY_P12 cannot be null or empty!")
        Preconditions.checkArgument(serviceAccountEmail.isNotEmpty(), "ApplicationConfig.SERVICE_ACCOUNT_EMAIL cannot be null or empty!")
        Preconditions.checkArgument(!userFractionSteps.any { it <= 0.00 || 1.00 < it }, "ApplicationConfig.USER_FRACTION_STEPS must be 0.00 < value <= 1.00")
        AndroidPublisherHelper.init(applicationName = applicationName, serviceAccountEmail = serviceAccountEmail, p12File = p12File)
    }

    fun currentTrackInfo(): Track {
        // Create the API service.
        val service = AndroidPublisherHelper.init(applicationName = applicationName, serviceAccountEmail = serviceAccountEmail, p12File = p12File)
        val edits = service.edits()

        // Create a new edit to make changes.
        val editRequest = edits.insert(packageName, null)
        val appEdit = editRequest.execute()

        // Get current track.
        return edits.tracks().get(packageName, appEdit.id, TrackType.Production.value).execute()
    }

    fun executeStepRelease(): StepReleaseResult {
        // Create the API service.
        val service = AndroidPublisherHelper.init(applicationName = applicationName, serviceAccountEmail = serviceAccountEmail, p12File = p12File)
        val edits = service.edits()

        // Create a new edit to make changes.
        val editRequest = edits.insert(packageName, null)
        val appEdit = editRequest.execute()

        // Get current track.
        val currentTrack = edits.tracks().get(packageName, appEdit.id, TrackType.Production.value).execute()

        // Get update information to be applied.
        val releaseUpdateType = classifyReleaseUpdateType(currentTrack)

        return if (releaseUpdateType != ReleaseUpdateType.None) {
            //update edit.
            val newTrack = generateReleaseUpdate(currentTrack, releaseUpdateType)
            edits.tracks().update(packageName, appEdit.id, TrackType.Production.value, newTrack).execute()

            // Commit changes for edit.
            edits.commit(packageName, appEdit.id).execute()

            StepReleaseResult.UpdatedTrack(newTrack, currentTrack)
        } else {
            StepReleaseResult.NoUpdatedTrack(currentTrack)
        }
    }

    private fun classifyReleaseUpdateType(track: Track): ReleaseUpdateType {
        track.releases.forEach { release ->
            if (release.status == ReleaseState.InProgress.value && release.userFraction != null && release.userFraction < 1.00) {
                userFractionSteps.sorted().forEach {
                    if (release.userFraction < it) {
                        return if (it < 1.00) {
                            ReleaseUpdateType.Expand(release.versionCodes, it)
                        } else {
                            ReleaseUpdateType.Complete(release.versionCodes)
                        }
                    }
                }
            }
        }
        return ReleaseUpdateType.None
    }

    private fun generateReleaseUpdate(track: Track, updateType: ReleaseUpdateType): Track {
        return track.clone().apply {
            when (updateType) {
                is ReleaseUpdateType.Expand -> {
                    //expand userFraction for target release.
                    releases.first { it.versionCodes == updateType.versionCodes }.apply {
                        userFraction = updateType.userFraction
                    }
                }
                is ReleaseUpdateType.Complete -> {
                    //change completed status for target release.
                    releases.first { it.versionCodes == updateType.versionCodes }.apply {
                        status = ReleaseState.Completed.value
                        userFraction = null
                    }
                    //remove status for old release.
                    releases.removeIf { it.status == ReleaseState.Completed.value && it.versionCodes != updateType.versionCodes }
                }
                is ReleaseUpdateType.None -> {
                    //do nothing.
                }
            }
        }
    }

}
