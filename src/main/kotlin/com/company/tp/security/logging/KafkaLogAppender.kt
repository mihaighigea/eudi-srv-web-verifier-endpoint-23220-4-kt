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

import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.config.SslConfigs
import java.util.Properties

class KafkaLogAppender : AppenderBase<ILoggingEvent>() {
    var topic: String = ""
    var bootstrapServers: String = ""
    var pattern: String = "%d{yyyy-MM-dd HH:mm:ss} %-5level %logger{36} - %msg%n"
    var sslKeystoreLocation: String? = null
    var sslKeystorePassword: String? = null
    var sslKeyPassword: String? = null
    var sslTruststoreLocation: String? = null
    var sslTruststorePassword: String? = null

    private lateinit var layout: PatternLayout
    private lateinit var producer: KafkaProducer<String, String>

    override fun start() {
        if (bootstrapServers.isBlank() || topic.isBlank()) {
            addWarn("KafkaLogAppender not configured (bootstrapServers/topic missing)")
            return
        }

        layout = PatternLayout().apply {
            context = this@KafkaLogAppender.context
            pattern = this@KafkaLogAppender.pattern
            start()
        }

        val props = Properties().apply {
            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
            put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL")
            sslKeystoreLocation?.let { put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, it) }
            sslKeystorePassword?.let { put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, it) }
            sslKeyPassword?.let { put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, it) }
            sslTruststoreLocation?.let { put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, it) }
            sslTruststorePassword?.let { put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, it) }
        }

        producer = KafkaProducer(props)
        super.start()
    }

    override fun append(eventObject: ILoggingEvent) {
        val msg = layout.doLayout(eventObject)
        producer.send(ProducerRecord(topic, msg))
    }

    override fun stop() {
        if (::producer.isInitialized) {
            producer.close()
        }
        super.stop()
    }
}
