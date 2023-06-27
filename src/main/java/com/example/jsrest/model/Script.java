package com.example.jsrest.model;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
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
    private String output;
    @Temporal(TemporalType.TIMESTAMP)
    @JsonView(Views.ShortInfo.class)
    private Date schedTime;
    @JsonView(Views.ShortInfo.class)
    private int execTime;

    public enum ScriptStatus {
        QUEUED, EXECUTING,
        COMPLETED {
            @Override
            public boolean isFinished() {
                return true;
            }
        },
        FAILED {
            @Override
            public boolean isFinished() {
                return true;
            }
        },
        INTERRUPTED {
            @Override
            public boolean isFinished() {
                return true;
            }
        };

        public boolean isFinished() {
            return false;
        }
    }

    public static class Views {
        public static class ShortInfo {}
    }
}
