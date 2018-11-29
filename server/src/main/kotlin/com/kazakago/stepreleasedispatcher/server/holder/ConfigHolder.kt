package com.kazakago.stepreleasedispatcher.server.holder

import com.kazakago.stepreleasedispatcher.config.ConfigLoader

object ConfigHolder {

    val INSTANCE = ConfigLoader.load()

    fun apply() {
        ConfigLoader.apply(INSTANCE)
    }

}