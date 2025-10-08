package com.ms.aspects.autoconfigure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter @Setter
@ConfigurationProperties(prefix = "aspects")
public class AspectsProperties {
    private boolean enabled = true;
    private String serviceName = "unknown-service";
    private String topic = "service_logs";
    private Metric metric = new Metric();
    private Cache cache = new Cache();

    @Getter @Setter
    public static class Metric {
        private long slowThresholdMs = 500;
    }

    @Getter @Setter
    public static class Cache {
        private long defaultTtlMs = 60000;
    }
}
