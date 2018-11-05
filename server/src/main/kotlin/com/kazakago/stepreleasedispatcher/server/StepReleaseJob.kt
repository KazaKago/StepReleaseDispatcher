package com.kazakago.stepreleasedispatcher.server

import com.kazakago.stepreleasedispatcher.config.ApplicationConfig
import com.kazakago.stepreleasedispatcher.notifier.NotificationProvider
import com.kazakago.stepreleasedispatcher.notifier.NotificationProviders
import com.kazakago.stepreleasedispatcher.releasedispatcher.ReleaseDispatcher
import org.quartz.Job
import org.quartz.JobExecutionContext

class StepReleaseJob : Job {

    private val notificationProviders = initializeNotificationProvider()

    override fun execute(context: JobExecutionContext) {
        try {
            val stepReleaseDispatcher = ReleaseDispatcher(
                    applicationName = ApplicationConfig.getApplicationName(),
                    packageName = ApplicationConfig.getPackageName(),
                    p12File = ApplicationConfig.getP12KeyFile(),
                    serviceAccountEmail = ApplicationConfig.getServiceAccountEmail(),
                    userFractionSteps = ApplicationConfig.getUserFractionStep())
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
                applicationName = ApplicationConfig.getApplicationName(),
                slackType = NotificationProvider.Type.Slack(ApplicationConfig.getSlackWebHookUrl()))
    }

}