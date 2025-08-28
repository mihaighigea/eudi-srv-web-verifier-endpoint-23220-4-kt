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
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import java.util.Properties

class KafkaLogAppender(
    private val config: KafkaLoggingProperties,
    private val producerProps: Properties,
) : AppenderBase<ILoggingEvent>() {

    private lateinit var layout: PatternLayout
    private lateinit var producer: KafkaProducer<String, String>

    override fun start() {
        super.start()
        layout = PatternLayout().also {
            it.context = context
            it.pattern = config.pattern
            it.start()
        }
        producer = KafkaProducer(producerProps)
    }

    override fun stop() {
        producer.close()
        super.stop()
    }

    override fun append(eventObject: ILoggingEvent) {
        val msg = layout.doLayout(eventObject)
        producer.send(ProducerRecord(config.topic, msg))
    }
}
