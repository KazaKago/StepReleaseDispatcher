package com.kazakago.stepreleasedispatcher.server

import com.kazakago.stepreleasedispatcher.config.ConfigLoader
import com.kazakago.stepreleasedispatcher.notifier.NotificationProvider
import com.kazakago.stepreleasedispatcher.notifier.NotificationProviders
import com.kazakago.stepreleasedispatcher.releasedispatcher.ReleaseDispatcher
import org.quartz.Job
import org.quartz.JobExecutionContext

class StepReleaseJob : Job {

    private val notificationProviders = initializeNotificationProvider()
    private val config = ConfigLoader.load()

    override fun execute(context: JobExecutionContext) {
        try {
            val stepReleaseDispatcher = ReleaseDispatcher(
                    applicationName = config.applicationName,
                    packageName = config.packageName,
                    p12File = ConfigLoader.getP12KeyFile(),
                    serviceAccountEmail = config.serviceAccountEmail,
                    userFractionSteps = config.userFractionStep)
            when (val result = stepReleaseDispatcher.executeStepRelease()) {
                is ReleaseDispatcher.StepReleaseResult.UpdatedTrack -> notificationProviders.postExpansionMessage(result.newTrack, result.oldTrack)
                is ReleaseDispatcher.StepReleaseResult.NoUpdatedTrack -> notificationProviders.postNoExpansionMessage(result.currentTrack)
            }
        } catch (exception: Exception) {
            notificationProviders.postErrorMessage(exception)
        }
    }

    private fun initializeNotificationProvider(): NotificationProviders {
        return NotificationProviders(
                applicationName = config.applicationName,
                slackType = NotificationProvider.Type.Slack(config.slackWebHookUrl)
        )
    }

}