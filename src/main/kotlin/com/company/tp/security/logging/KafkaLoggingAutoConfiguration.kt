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

import ch.qos.logback.classic.AsyncAppender
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.filter.ThresholdFilter
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.SslConfigs
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.util.Properties

@Configuration
@ConditionalOnProperty(prefix = "logging.kafka", name = ["enabled"], havingValue = "true")
@EnableConfigurationProperties(KafkaLoggingProperties::class)
class KafkaLoggingAutoConfiguration(
    private val properties: KafkaLoggingProperties,
) {

    init {
        val context = LoggerFactory.getILoggerFactory() as LoggerContext

        val producerProps = Properties().apply {
            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.bootstrapServers)
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
            put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL")
            properties.ssl.keystoreLocation?.let { put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, it) }
            properties.ssl.keystorePassword?.let { put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, it) }
            properties.ssl.keyPassword?.let { put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, it) }
            properties.ssl.truststoreLocation?.let { put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, it) }
            properties.ssl.truststorePassword?.let { put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, it) }
        }

        val kafkaAppender = KafkaLogAppender(properties, producerProps).apply {
            this.context = context
            start()
        }

        val asyncAppender = AsyncAppender().apply {
            this.context = context
            queueSize = properties.queueSize
            discardingThreshold = properties.discardingThreshold
            addAppender(kafkaAppender)
            addFilter(
                ThresholdFilter().apply {
                    setLevel(properties.level)
                    start()
                },
            )
            start()
        }

        context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).addAppender(asyncAppender)
    }
}
