package com.kazakago.stepreleasedispatcher.releasedispatcher

enum class ReleaseState(val value: String) {
    Completed("completed"),
    Draft("draft"),
    Halted("halted"),
    InProgress("inProgress"),
}