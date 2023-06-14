package com.example.jsrest.model;

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
    private long id;
    private String body;
    private ScriptStatus status = ScriptStatus.QUEUED;
    private String output;
    private String errors;
    @Temporal(TemporalType.TIMESTAMP)
    private Date schedTime;
    private int execTime;

    public enum ScriptStatus {
        QUEUED, EXCECUTING,
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
}
