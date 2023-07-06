package com.example.jsrest.streams;

import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.IOException;
import java.io.OutputStream;

/**
 * {@code OutputStream} realization that sends data to the bounded {@code ResponseBodyEmitter}
 */
public final class ResponseEmitterOutputStream extends OutputStream
{
    private final ResponseBodyEmitter emitter;

    public ResponseEmitterOutputStream(ResponseBodyEmitter emitter) {
        this.emitter = emitter;
    }

    @Override
    public synchronized void write(int b) throws IOException {
        emitter.send((char)b);
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        emitter.send(new String(b, off, len));
    }

    @Override
    public synchronized void close() {
        emitter.complete();
    }
}
