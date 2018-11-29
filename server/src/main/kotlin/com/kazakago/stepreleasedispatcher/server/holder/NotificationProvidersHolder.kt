package com.kazakago.stepreleasedispatcher.server.holder

import com.kazakago.stepreleasedispatcher.notifier.NotificationProvider
import com.kazakago.stepreleasedispatcher.notifier.NotificationProviders

object NotificationProvidersHolder {

    val INSTANCE = NotificationProviders(
        applicationName = ConfigHolder.INSTANCE.applicationName,
        slackType = NotificationProvider.Type.Slack(ConfigHolder.INSTANCE.slackWebHookUrl)
    )

}