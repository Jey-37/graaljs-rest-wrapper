package com.example.jsrest.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter @Setter
public class ScriptDto
{
    private long id;
    private String body;
    private String status;
    private Date schedTime;
    private int execTime;
}
