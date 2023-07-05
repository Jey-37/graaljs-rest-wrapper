package com.example.jsrest.streams;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.time.Instant;

public class DelayedOutputStream extends FilterOutputStream
{
    protected final ByteArrayOutputStream buf;

    protected final int delay;

    protected Instant lastWriteTime;

    public DelayedOutputStream(OutputStream out, int delay) {
        super(out);
        if (delay < 0) {
            throw new IllegalArgumentException("Negative delay");
        }
        this.delay = delay;
        this.buf = new ByteArrayOutputStream(2048);
        this.lastWriteTime = Instant.now();
    }

    private void flushBuffer() throws IOException {
        if (buf.size() > 0) {
            buf.writeTo(out);
            buf.reset();
            lastWriteTime = Instant.now();
        }
    }

    @Override
    public synchronized void write(int b) throws IOException {
        buf.write(b);
        if (Duration.between(lastWriteTime, Instant.now()).toMillis() >= delay) {
            flushBuffer();
        }
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        buf.write(b, off, len);
        if (Duration.between(lastWriteTime, Instant.now()).toMillis() >= delay) {
            flushBuffer();
        }
    }

    @Override
    public synchronized void flush() throws IOException {
        flushBuffer();
        out.flush();
    }
}
