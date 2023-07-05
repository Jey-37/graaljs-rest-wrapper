package com.example.jsrest.streams;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class OutputStreamDistributor extends OutputStream
{
    private final Set<OutputStream> streams = new CopyOnWriteArraySet<>();

    public OutputStreamDistributor(Collection<OutputStream> outputStreams) {
        streams.addAll(outputStreams);
    }

    public OutputStreamDistributor(OutputStream... outputStreams) {
        streams.addAll(Set.of(outputStreams));
    }

    public void add(OutputStream outputStream) {
        streams.add(outputStream);
    }

    public void remove(OutputStream outputStream) {
        streams.remove(outputStream);
    }

    @Override
    public void write(int b) throws IOException {
        for (var stream : streams) {
            stream.write(b);
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        for (var stream : streams) {
            stream.write(b, off, len);
        }
    }

    @Override
    public void flush() throws IOException {
        for (var stream : streams) {
            stream.flush();
        }
    }

    @Override
    public void close() throws IOException {
        for (var stream : streams) {
            stream.close();
        }
    }
}
