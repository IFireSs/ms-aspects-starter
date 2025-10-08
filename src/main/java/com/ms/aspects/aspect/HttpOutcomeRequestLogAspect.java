package com.ms.aspects.aspect;

import com.ms.aspects.annotations.HttpOutcomeRequestLog;
import com.ms.aspects.dto.HttpRequestEvent;
import com.ms.aspects.kafka.LoggingKafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpEntity;
import org.springframework.http.RequestEntity;

import java.net.URI;
import java.time.Instant;

@Aspect
@RequiredArgsConstructor
@Slf4j
public class HttpOutcomeRequestLogAspect {

    private final LoggingKafkaProducer loggingKafkaProducer;

    @AfterReturning(pointcut = "@annotation(ann)", returning = "retVal")
    public void after(JoinPoint jp, HttpOutcomeRequestLog ann, Object retVal) {
        MethodSignature sig = (MethodSignature) jp.getSignature();
        String methodSignature = sig.toLongString();

        String uri = null; Object body = null;
        for (Object arg : jp.getArgs()) {
            if (arg == null) continue;
            if (arg instanceof URI u) {
                uri = u.toString();
            } else if (arg instanceof String s && (s.startsWith("http://") || s.startsWith("https://"))) {
                uri = s;
            } else if (arg instanceof RequestEntity<?> re) {
                URI u = re.getUrl();
                if (u != null) uri = u.toString();
                body = re.getBody();
            } else if (arg instanceof HttpEntity<?> he) {
                body = he.getBody();
            }
        }

        var event = HttpRequestEvent.builder()
                .timestamp(Instant.now())
                .method(methodSignature)
                .direction("OUT")
                .uri(uri)
                .params(jp.getArgs())
                .body(body)
                .result(retVal)
                .build();
        try {
            loggingKafkaProducer.send(ann.value(), event);
        } catch (Exception e) {
            log.info("Kafka unavailable for OUT http log: {}", e.getMessage());
        }
    }
}
