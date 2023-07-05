package com.example.jsrest.model;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

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
    @Column(length = 5000)
    private String output;
    @Temporal(TemporalType.TIMESTAMP)
    @JsonView(Views.ShortInfo.class)
    private Date schedTime;
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
            return (int)(new Date().getTime()-schedTime.getTime());
        }
        return execTime;
    }
}
