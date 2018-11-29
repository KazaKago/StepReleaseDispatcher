package com.kazakago.stepreleasedispatcher.scheduler

import org.quartz.*
import org.quartz.impl.StdSchedulerFactory
import kotlin.reflect.KClass

class StepReleaseJobScheduler<T : Job>(dispatchSchedule: String, jobClass: KClass<T>) {

    private var nativeScheduler: Scheduler? = null
    var dispatchSchedule = dispatchSchedule
        set(value) {
            if (isActive()) start()
            field = value
        }
    var jobClass = jobClass
        set(value) {
            if (isActive()) start()
            field = value
        }

    private fun initializeScheduler(): Scheduler {
        val scheduler = StdSchedulerFactory.getDefaultScheduler()!!
        val job = JobBuilder.newJob(jobClass.java)
            .withIdentity("StepReleaseJob", "StepReleaseJobScheduler")
            .build()
        val trigger = TriggerBuilder.newTrigger()
            .withIdentity("StepReleaseJobTrigger", "StepReleaseJobScheduler")
            .withSchedule(CronScheduleBuilder.cronSchedule(dispatchSchedule))
            .build()
        scheduler.scheduleJob(job, trigger)
        return scheduler
    }

    fun start() {
        nativeScheduler?.shutdown()
        nativeScheduler = initializeScheduler()
        nativeScheduler?.start()
    }

    fun shutdown() {
        nativeScheduler?.shutdown()
    }

    fun isActive(): Boolean {
        return nativeScheduler?.isStarted == true && nativeScheduler?.isShutdown == false
    }

}