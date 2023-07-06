package com.example.jsrest.streams;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * {@code OutputStream} decorator that allows blocking of
 * the underlying stream. When a stream is blocked, no actions can be performed
 * with it until it is unblocked again.
 */
public class BlockableOutputStream extends FilterOutputStream
{
    private final AtomicBoolean blocked = new AtomicBoolean();

    public BlockableOutputStream(OutputStream out) {
        super(out);
    }

    public boolean isBlocked() {
        return blocked.get();
    }

    public void block() {
        blocked.set(true);
    }

    public void unblock() {
        blocked.set(false);
    }

    @Override
    public synchronized void write(int b) throws IOException {
        if (!blocked.get()) {
            out.write(b);
        }
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        Objects.checkFromIndexSize(off, len, b.length);
        if (!blocked.get()) {
            out.write(b, off, len);
        }
    }

    @Override
    public synchronized void flush() throws IOException {
        if (!blocked.get()) {
            out.flush();
        }
    }

    @Override
    public synchronized void close() throws IOException {
        if (!blocked.get()) {
            super.close();
        }
    }
}
