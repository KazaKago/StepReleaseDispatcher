/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kazakago.stepreleasedispatcher.config

import java.io.File
import java.io.InputStream
import java.util.prefs.Preferences

/**
 * Contains global application configuration.
 */
object ApplicationConfig {

    private val preferences = Preferences.userNodeForPackage(this.javaClass)

    /**
     * Specify the name of your application. If the application name is
     * `null` or blank, the application will log a warning. Suggested
     * format is "MyCompany-Application/1.0".
     */
    private const val ApplicationName = "ApplicationName"

    fun putApplicationName(value: String) {
        preferences.put(ApplicationName, value)
    }

    fun getApplicationName(): String {
        return preferences.get(ApplicationName, getDefaultApplicationName())
    }

    fun getDefaultApplicationName(): String {
        return "StepReleaseDispatcher"
    }

    /**
     * Specify the package name of the app.
     */
    private const val PackageName = "PackageName"

    fun putPackageName(value: String) {
        preferences.put(PackageName, value)
    }

    fun getPackageName(): String {
        return preferences.get(PackageName, getDefaultPackageName())
    }

    fun getDefaultPackageName(): String {
        return ""
    }

    /**
     * Authentication.
     * Enter the service account email and add your key.p12 file to the resources directory.
     */
    private const val ServiceAccountEmail = "ServiceAccountEmail"

    fun putServiceAccountEmail(value: String) {
        preferences.put(ServiceAccountEmail, value)
    }

    fun getServiceAccountEmail(): String {
        return preferences.get(ServiceAccountEmail, getDefaultServiceAccountEmail())
    }

    fun getDefaultServiceAccountEmail(): String {
        return ""
    }

    /**
     * Path to the private key file.
     */
    private const val P12KeyFilePath = "key.p12"

    fun putP12KeyFile(value: InputStream) {
        val file = File(P12KeyFilePath)
        file.outputStream().buffered().use { outputStream ->
            value.copyTo(outputStream)
        }
    }

    fun getP12KeyFile(): File {
        return File(P12KeyFilePath)
    }

    /**
     * Input the time when the process is executed in cron style
     */
    private const val DispatchSchedule = "DispatchSchedule"

    fun putDispatchSchedule(value: String) {
        preferences.put(DispatchSchedule, value)
    }

    fun getDispatchSchedule(): String {
        return preferences.get(DispatchSchedule, getDefaultDispatchSchedule())
    }

    fun getDefaultDispatchSchedule(): String {
        return "0 0 11 * * ?" // Fire at 11:00am every day
    }

    /**
     * User Fraction Steps.
     * set 0.00 < fraction <= 1.00.
     */
    private const val UserFractionStep = "UserFractionStep"

    fun putUserFractionStep(value: List<Double>) {
        preferences.put(UserFractionStep, value.joinToString(","))
    }

    fun getUserFractionStep(): List<Double> {
        return preferences.get(UserFractionStep, getDefaultFractionStep().joinToString(",")).split(",").map { it.toDouble() }
    }

    fun getDefaultFractionStep(): List<Double> {
        return listOf(0.01, 0.02, 0.05, 0.10, 0.20, 0.50, 1.00)
    }

    private fun getFileFromResourceFolder(filePath: String): File {
        val url = ClassLoader.getSystemResource(filePath)
        return File(url.path)
    }

    /**
     * Input slack webhook url if you notify updating user fraction.
     */
    private const val SlackWebHookUrl = "SlackWebHookUrl"

    fun putSlackWebHookUrl(value: String) {
        preferences.put(SlackWebHookUrl, value)
    }

    fun getSlackWebHookUrl(): String {
        return preferences.get(SlackWebHookUrl, getDefaultSlackWebHookUrl())
    }

    fun getDefaultSlackWebHookUrl(): String {
        return ""
    }

}