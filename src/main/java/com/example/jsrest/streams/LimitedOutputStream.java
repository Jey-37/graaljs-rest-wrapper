package com.example.jsrest.streams;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

public class LimitedOutputStream extends FilterOutputStream
{
    protected final int limit;

    protected int count;

    public LimitedOutputStream(OutputStream out, int limit) {
        super(out);
        if (limit < 0) {
            throw new IllegalArgumentException("Negative limit");
        }
        this.limit = limit;
    }

    @Override
    public synchronized void write(int b) throws IOException {
        if (limit > 0 && count == limit) {
            return;
        }
        out.write(b);
        count++;
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        Objects.checkFromIndexSize(off, len, b.length);
        if (limit > 0 && count + len > limit) {
            return;
        }
        out.write(b, off, len);
        count += len;
    }
}
