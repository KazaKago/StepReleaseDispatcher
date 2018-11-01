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

package com.kazakago.stepreleasedispatcher.releasedispatcher

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.repackaged.com.google.common.base.Preconditions
import com.google.api.services.androidpublisher.AndroidPublisher
import com.google.api.services.androidpublisher.AndroidPublisherScopes
import java.io.File
import java.io.IOException
import java.security.GeneralSecurityException

/**
 * Helper class to initialize the publisher APIs client library.
 */
object AndroidPublisherHelper {

    /** Global instance of the JSON factory.  */
    private val JsonFactory by lazy { JacksonFactory.getDefaultInstance() }
    /** Global instance of the HTTP transport.  */
    private val HttpTransport by lazy { GoogleNetHttpTransport.newTrustedTransport() }

    private fun authorizeWithServiceAccount(serviceAccountEmail: String, p12File: File): Credential {
        // Build service account credential.
        return GoogleCredential.Builder()
                .setTransport(HttpTransport)
                .setJsonFactory(JsonFactory)
                .setServiceAccountId(serviceAccountEmail)
                .setServiceAccountScopes(setOf(AndroidPublisherScopes.ANDROIDPUBLISHER))
                .setServiceAccountPrivateKeyFromP12File(p12File)
                .build()
    }

    /**
     * Performs all necessary setup steps for running requests against the API.
     *
     * @param applicationName the name of the application: com.example.app
     * @param serviceAccountEmail the Service Account Email
     * @param p12File the p12 file
     * @return the {@Link AndroidPublisher} service
     * @throws GeneralSecurityException
     * @throws IOException
     */
    fun init(applicationName: String, serviceAccountEmail: String, p12File: File): AndroidPublisher {
        Preconditions.checkArgument(applicationName.isNotEmpty(), "applicationName cannot be null or empty!")

        // Authorization.
        val credential = authorizeWithServiceAccount(serviceAccountEmail, p12File)

        // Set up and return API client.
        return AndroidPublisher.Builder(HttpTransport, JsonFactory, credential)
                .setApplicationName(applicationName)
                .build()
    }

}