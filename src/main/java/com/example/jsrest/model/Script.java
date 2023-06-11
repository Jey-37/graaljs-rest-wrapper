package com.example.jsrest.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;

import java.util.Date;

@Entity
@Data
public class Script
{
    @Id
    private long id;
    private String body;
    private String status;
    private String output;
    private String errors;
    @Temporal(TemporalType.TIMESTAMP)
    private Date schedTime;
    @Temporal(TemporalType.TIMESTAMP)
    private Date execTime;
}
