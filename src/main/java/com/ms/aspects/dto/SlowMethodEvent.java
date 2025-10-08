package com.ms.aspects.dto;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data @Builder
public class SlowMethodEvent {
    private Instant timestamp;
    private String method;
    private Long durationMs;
    private Object[] params;
}
