package com.kazakago.stepreleasedispatcher.config

import com.squareup.moshi.Json

data class Config(
        /**
         * Specify the name of your application. If the application name is
         * `null` or blank, the application will log a warning. Suggested
         * format is "MyCompany-Application/1.0".
         */
        @Json(name = "application_name")
        var applicationName: String = "StepReleaseDispatcher",
        /**
         * Specify the package name of the app.
         */
        @Json(name = "package_name")
        var packageName: String = "",
        /**
         * Authentication.
         * Enter the service account email and add your key.p12 file to the resources directory.
         */
        @Json(name = "service_account_email")
        var serviceAccountEmail: String = "",
        /**
         * Input the time when the process is executed in cron style
         */
        @Json(name = "dispatch_schedule")
        var dispatchSchedule: String = "0 0 11 * * ?",
        /**
         * User Fraction Steps.
         * set 0.00 < fraction <= 1.00.
         */
        @Transient
        @Json(name = "user_fraction_step")
        var userFractionStep: List<Double> = listOf(0.01, 0.02, 0.05, 0.10, 0.20, 0.50, 1.00),
        /**
         * Input slack webhook url if you notify updating user fraction.
         */
        @Json(name = "slack_web_hook_url")
        var slackWebHookUrl: String = ""
)