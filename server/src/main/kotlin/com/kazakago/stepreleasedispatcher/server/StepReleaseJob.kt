package com.kazakago.stepreleasedispatcher.server

import com.google.api.services.androidpublisher.model.Track
import com.kazakago.stepreleasedispatcher.config.ApplicationConfig
import com.kazakago.stepreleasedispatcher.notifier.NotificationProvider
import com.kazakago.stepreleasedispatcher.notifier.NotificationProviders
import com.kazakago.stepreleasedispatcher.releasedispatcher.ReleaseDispatcher
import org.quartz.Job
import org.quartz.JobExecutionContext

class StepReleaseJob : Job {

    private val notificationProviders = initializeNotificationProvider()

    override fun execute(context: JobExecutionContext) {
        val stepReleaseDispatcher = ReleaseDispatcher(
                applicationName = ApplicationConfig.getApplicationName(),
                packageName = ApplicationConfig.getPackageName(),
                p12File = ApplicationConfig.getP12KeyFile(),
                serviceAccountEmail = ApplicationConfig.getServiceAccountEmail(),
                userFractionSteps = ApplicationConfig.getUserFractionStep()
        )
        stepReleaseDispatcher.onUpdatedTrack = { newTrack: Track, oldTrack: Track ->
            notificationProviders.postExpansionMessage(newTrack, oldTrack)
        }
        stepReleaseDispatcher.onNoUpdatedTrack = { currentTrack: Track ->
            notificationProviders.postNoExpansionMessage(currentTrack)
        }
        stepReleaseDispatcher.onError = { exception ->
            notificationProviders.postErrorMessage(exception)
        }
        stepReleaseDispatcher.executeStepRelease()
    }

    private fun initializeNotificationProvider(): NotificationProviders {
        return NotificationProviders(
                applicationName = ApplicationConfig.getApplicationName(),
                slackType = NotificationProvider.Type.Slack(ApplicationConfig.getSlackWebHookUrl()))
    }

}