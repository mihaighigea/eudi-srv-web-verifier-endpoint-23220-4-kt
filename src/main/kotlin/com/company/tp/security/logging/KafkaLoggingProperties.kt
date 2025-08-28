/*
 * Copyright (c) 2023 European Commission
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.company.tp.security.logging

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("logging.kafka")
data class KafkaLoggingProperties(
    val enabled: Boolean = false,
    val topic: String = "",
    val bootstrapServers: String = "",
    val queueSize: Int = 256,
    val discardingThreshold: Int = 0,
    val pattern: String = "%d{yyyy-MM-dd HH:mm:ss} %-5level %logger{36} - %msg%n",
    val level: String = "INFO",
    val ssl: SslProperties = SslProperties(),
) {
    data class SslProperties(
        val keystoreLocation: String? = null,
        val keystorePassword: String? = null,
        val keyPassword: String? = null,
        val truststoreLocation: String? = null,
        val truststorePassword: String? = null,
    )
}
