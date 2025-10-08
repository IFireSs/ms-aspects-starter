package com.ms.aspects.aspect;

import com.ms.aspects.annotations.HttpIncomeRequestLog;
import com.ms.aspects.dto.HttpRequestEvent;
import com.ms.aspects.kafka.LoggingKafkaProducer;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;

@Aspect
@RequiredArgsConstructor
@Slf4j
public class HttpIncomeRequestLogAspect {

    private final LoggingKafkaProducer loggingKafkaProducer;

    @Before("@annotation(ann)")
    public void before(JoinPoint jp, HttpIncomeRequestLog ann) {
        MethodSignature sig = (MethodSignature) jp.getSignature();
        String methodSignature = sig.toLongString();

        String uri = null; String query = null; String httpMethod = null;
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes sra) {
            HttpServletRequest req = sra.getRequest();
            uri = req.getRequestURI();
            query = req.getQueryString();
            httpMethod = req.getMethod();
        }
        var event = HttpRequestEvent.builder()
                .timestamp(Instant.now())
                .method(methodSignature)
                .direction("IN")
                .uri(uri)
                .query(query)
                .httpMethod(httpMethod)
                .params(jp.getArgs())
                .build();
        try {
            loggingKafkaProducer.send(ann.value(), event);
        } catch (Exception e) {
            log.info("Kafka unavailable for IN http log: {}", e.getMessage());
        }
    }
}
