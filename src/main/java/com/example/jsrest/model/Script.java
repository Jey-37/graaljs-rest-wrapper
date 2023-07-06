package com.example.jsrest.model;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "scripts")
public class Script
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(Views.ShortInfo.class)
    private long id;
    @JsonView(Views.ShortInfo.class)
    private String body;
    @JsonView(Views.ShortInfo.class)
    private ScriptStatus status = ScriptStatus.QUEUED;
    @Column(length = 10000)
    private String output;
    @JsonView(Views.ShortInfo.class)
    private LocalDateTime pubTime = LocalDateTime.now();
    @JsonView(Views.ShortInfo.class)
    private LocalDateTime schedTime;
    @JsonView(Views.ShortInfo.class)
    private int execTime;

    public enum ScriptStatus {
        QUEUED, EXECUTING,
        COMPLETED, FAILED, INTERRUPTED;

        public boolean isFinished() {
            return switch (this) {
                case COMPLETED, FAILED, INTERRUPTED -> true;
                default -> false;
            };
        }
    }

    public static class Views {
        public static class ShortInfo {}
    }

    public Script(String body) {
        this.body = body;
    }

    public int getExecTime() {
        if (schedTime != null && execTime == 0) {
            return (int)Duration.between(schedTime, LocalDateTime.now()).toMillis();
        }
        return execTime;
    }
}
