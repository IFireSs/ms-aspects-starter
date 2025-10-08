package com.ms.aspects.dto;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data @Builder
public class HttpRequestEvent {
    private Instant timestamp;
    private String method;
    private String direction;
    private String uri;
    private String query;
    private String httpMethod;
    private Object[] params;
    private Object body;
    private Object result;
}
