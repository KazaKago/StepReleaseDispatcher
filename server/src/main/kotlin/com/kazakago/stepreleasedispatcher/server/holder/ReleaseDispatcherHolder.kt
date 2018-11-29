package com.kazakago.stepreleasedispatcher.server.holder

import com.kazakago.stepreleasedispatcher.config.ConfigLoader
import com.kazakago.stepreleasedispatcher.releasedispatcher.ReleaseDispatcher

object ReleaseDispatcherHolder {

    val INSTANCE = ReleaseDispatcher(
        applicationName = ConfigHolder.INSTANCE.applicationName,
        packageName = ConfigHolder.INSTANCE.packageName,
        p12File = ConfigLoader.getP12KeyFile(),
        serviceAccountEmail = ConfigHolder.INSTANCE.serviceAccountEmail,
        userFractionSteps = ConfigHolder.INSTANCE.userFractionStep
    )

}
