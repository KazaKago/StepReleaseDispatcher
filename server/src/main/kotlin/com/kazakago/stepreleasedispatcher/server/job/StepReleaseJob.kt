package com.kazakago.stepreleasedispatcher.server.job

import com.kazakago.stepreleasedispatcher.releasedispatcher.ReleaseDispatcher
import com.kazakago.stepreleasedispatcher.server.holder.NotificationProvidersHolder
import com.kazakago.stepreleasedispatcher.server.holder.ReleaseDispatcherHolder
import org.quartz.Job
import org.quartz.JobExecutionContext

class StepReleaseJob : Job {

    override fun execute(context: JobExecutionContext) {
        try {
            when (val result = ReleaseDispatcherHolder.INSTANCE.executeStepRelease()) {
                is ReleaseDispatcher.StepReleaseResult.UpdatedTrack -> NotificationProvidersHolder.INSTANCE.postExpansionMessage(result.newTrack, result.oldTrack)
                is ReleaseDispatcher.StepReleaseResult.NoUpdatedTrack -> NotificationProvidersHolder.INSTANCE.postNoExpansionMessage(result.currentTrack)
            }
        } catch (exception: Exception) {
            NotificationProvidersHolder.INSTANCE.postErrorMessage(exception)
        }
    }

}