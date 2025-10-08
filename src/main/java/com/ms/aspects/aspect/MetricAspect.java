package com.ms.aspects.aspect;

import com.ms.aspects.annotations.Metric;
import com.ms.aspects.autoconfigure.AspectsProperties;
import com.ms.aspects.dto.SlowMethodEvent;
import com.ms.aspects.kafka.LoggingKafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Aspect
@RequiredArgsConstructor
@Slf4j
public class MetricAspect {

    private final LoggingKafkaProducer kafka;
    private final AspectsProperties properties;

    @Around("@annotation(metric)")
    public Object around(ProceedingJoinPoint pjp, Metric metric) throws Throwable {
        long start = System.nanoTime();
        Object result = pjp.proceed();
        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        long threshold = metric.thresholdMs() > -1 ? metric.thresholdMs() : properties.getMetric().getSlowThresholdMs();
        if (threshold > 0 && tookMs >= threshold) {
            String methodSig = ((MethodSignature) pjp.getSignature()).toLongString();
            var event = SlowMethodEvent.builder()
                    .timestamp(Instant.now())
                    .method(metric.value().isBlank() ? methodSig : metric.value())
                    .durationMs(tookMs)
                    .params(metric.includeParams() ? pjp.getArgs() : null)
                    .build();
            try {
                kafka.send("WARNING", event);
            } catch (Exception e) {
                log.warn("Failed to send WARNING metric to Kafka: {}", e.getMessage());
            }
        }
        return result;
    }
}
