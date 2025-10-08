package com.ms.aspects.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoggingKafkaProducer {

    private final ObjectProvider<KafkaTemplate<String, String>> kafkaTemplateProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${aspects.topic:service_logs}")
    private String topic;

    @Value("${aspects.service-name:unknown-service}")
    private String serviceName;

    public void send(String typeHeader, Object payload) {
        KafkaTemplate<String, String> kafkaTemplate = kafkaTemplateProvider.getIfAvailable();
        if (kafkaTemplate == null) {
            throw new IllegalStateException("KafkaTemplate is not configured");
        }
        try {
            String json = (payload instanceof String) ? (String) payload : objectMapper.writeValueAsString(payload);
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, serviceName, json);
            record.headers().add(new RecordHeader("type", typeHeader.getBytes(StandardCharsets.UTF_8)));
            kafkaTemplate.send(record).whenComplete((res, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish service log to Kafka: {}", ex.toString());
                } else {
                    log.debug("Service log sent: topic={} partition={} offset={}",
                            res.getRecordMetadata().topic(), res.getRecordMetadata().partition(), res.getRecordMetadata().offset());
                }
            });
        } catch (Exception e) {
            log.error("Kafka publish failed: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
