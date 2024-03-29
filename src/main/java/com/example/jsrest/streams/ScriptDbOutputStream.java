package com.example.jsrest.streams;

import com.example.jsrest.service.ScriptDbService;

import java.io.OutputStream;

/**
 * {@code OutputStream} realization that appends data to the output of the bounded Script in the DB.
 */
public final class ScriptDbOutputStream extends OutputStream
{
    private final long scriptId;

    private final ScriptDbService dbService;

    public ScriptDbOutputStream(long scriptId, ScriptDbService dbService) {
        this.scriptId = scriptId;
        this.dbService = dbService;
    }

    @Override
    public synchronized void write(int b) {
        dbService.appendOutput(scriptId, String.valueOf(b));
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) {
        dbService.appendOutput(scriptId, new String(b, off, len));
    }
}
