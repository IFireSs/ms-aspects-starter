package com.ms.aspects.autoconfigure;

import com.ms.aspects.aspect.*;
import com.ms.aspects.cache.TtlCacheManager;
import com.ms.aspects.entity.ErrorLog;
import com.ms.aspects.kafka.LoggingKafkaProducer;
import com.ms.aspects.repository.ErrorLogRepository;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@AutoConfiguration
@EnableConfigurationProperties(AspectsProperties.class)
@ConditionalOnProperty(prefix = "aspects", name = "enabled", havingValue = "true", matchIfMissing = true)
@Import({ LoggingKafkaProducer.class, TtlCacheManager.class })
@EntityScan(basePackageClasses = ErrorLog.class)
@EnableJpaRepositories(basePackageClasses = ErrorLogRepository.class)
public class AspectsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public LogDatasourceErrorAspect logDatasourceErrorAspect(LoggingKafkaProducer kafka, ErrorLogRepository repo, AspectsProperties props) {
        return new LogDatasourceErrorAspect(kafka, repo, props);
    }

    @Bean
    @ConditionalOnMissingBean
    public MetricAspect metricAspect(LoggingKafkaProducer kafka, AspectsProperties props) {
        return new MetricAspect(kafka, props);
    }

    @Bean
    @ConditionalOnMissingBean
    public HttpOutcomeRequestLogAspect httpOutcomeRequestLogAspect(LoggingKafkaProducer kafka) {
        return new HttpOutcomeRequestLogAspect(kafka);
    }

    @Bean
    @ConditionalOnMissingBean
    public HttpIncomeRequestLogAspect httpIncomeRequestLogAspect(LoggingKafkaProducer kafka) {
        return new HttpIncomeRequestLogAspect(kafka);
    }

    @Bean
    @ConditionalOnMissingBean
    public CachedAspect cachedAspect(TtlCacheManager cacheManager, AspectsProperties props) {
        return new CachedAspect(cacheManager, props);
    }
}
