package com.kazakago.stepreleasedispatcher.server.holder

import com.kazakago.stepreleasedispatcher.scheduler.StepReleaseJobScheduler
import com.kazakago.stepreleasedispatcher.server.job.StepReleaseJob

object StepReleaseSchedulerHolder {

    val INSTANCE = StepReleaseJobScheduler(
            dispatchSchedule = ConfigHolder.INSTANCE.dispatchSchedule,
            jobClass = StepReleaseJob::class)

}