package com.ms.aspects.dto;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data @Builder
public class DatasourceErrorEvent {
    private Instant timestamp;
    private String method;
    private String exception;
    private String stacktrace;
    private Object[] params;
}
