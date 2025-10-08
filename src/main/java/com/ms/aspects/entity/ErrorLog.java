package com.ms.aspects.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "error_log")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ErrorLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(length = 512)
    private String methodSignature;

    @Column(length = 512)
    private String exceptionMessage;

    @Column(length = 8000)
    private String stacktrace;

    @Column(length = 4000)
    private String paramsJson;

    @Column(length = 128)
    private String serviceName;

    @Column(length = 32)
    private String type; // ERROR/WARNING/INFO
}
