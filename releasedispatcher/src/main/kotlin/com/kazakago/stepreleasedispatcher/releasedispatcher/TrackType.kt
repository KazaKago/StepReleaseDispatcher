package com.kazakago.stepreleasedispatcher.releasedispatcher

enum class TrackType(val value: String) {
    Track("alpha"),
    Beta("beta"),
    Production("production"),
    Rollout("rollout"),
    Internal("internal"),
}