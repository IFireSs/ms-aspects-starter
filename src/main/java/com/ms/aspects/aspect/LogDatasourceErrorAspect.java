package com.ms.aspects.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ms.aspects.annotations.LogDatasourceError;
import com.ms.aspects.autoconfigure.AspectsProperties;
import com.ms.aspects.dto.DatasourceErrorEvent;
import com.ms.aspects.entity.ErrorLog;
import com.ms.aspects.kafka.LoggingKafkaProducer;
import com.ms.aspects.repository.ErrorLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;

@Aspect
@RequiredArgsConstructor
@Slf4j
public class LogDatasourceErrorAspect {

    private final LoggingKafkaProducer kafka;
    private final ErrorLogRepository errorLogRepository;
    private final AspectsProperties properties;
    private final ObjectMapper mapper = new ObjectMapper();

    @Around("@annotation(ann)")
    public Object around(ProceedingJoinPoint pjp, LogDatasourceError ann) throws Throwable {
        try {
            return pjp.proceed();
        } catch (Throwable ex) {
            MethodSignature sig = (MethodSignature) pjp.getSignature();
            String methodSig = sig.toLongString();
            var event = DatasourceErrorEvent.builder()
                    .timestamp(Instant.now())
                    .method(methodSig)
                    .exception(ex.getMessage())
                    .stacktrace(stackToString(ex))
                    .params(pjp.getArgs())
                    .build();
            log.error("Datasource error in {}: {}", methodSig, ex.getMessage(), ex);
            boolean kafkaOk = true;
            try {
                kafka.send(ann.value(), event);
            } catch (Exception e) {
                kafkaOk = false;
            }
            if (!kafkaOk) {
                try {
                    String paramsJson = mapper.writeValueAsString(pjp.getArgs());
                    errorLogRepository.save(ErrorLog.builder()
                            .createdAt(Instant.now())
                            .methodSignature(methodSig)
                            .exceptionMessage(ex.getMessage())
                            .stacktrace(stackToString(ex))
                            .paramsJson(paramsJson)
                            .serviceName(properties.getServiceName())
                            .type(ann.value())
                            .build());
                } catch (Exception dbEx) {
                    log.error("Failed to store error log to DB: {}", dbEx.getMessage(), dbEx);
                }
            }
            throw ex;
        }
    }

    private String stackToString(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
