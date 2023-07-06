package com.example.jsrest.service;

import com.example.jsrest.model.Script;
import com.example.jsrest.model.Script.ScriptStatus;
import com.example.jsrest.streams.*;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class ScriptTask implements Runnable
{
    private volatile boolean isStarted;

    private AtomicBoolean isExecuted = new AtomicBoolean();

    private final ScriptDbService dbService;

    private Script script;

    private final Context context;

    private final OutputStream outputStream;

    private final BlockableOutputStream controlledStream;

    public ScriptTask(Script script, ScriptDbService dbService, OutputStream outputStream) {
        this.script = Objects.requireNonNull(script);
        this.dbService = Objects.requireNonNull(dbService);
        this.outputStream = createOutputStream(script, outputStream);
        this.controlledStream = new BlockableOutputStream(
                new LimitedOutputStream(this.outputStream, 5000));

        context = Context.newBuilder("js")
                .out(controlledStream).err(controlledStream)
                .allowHostAccess(HostAccess.NONE)
                .allowExperimentalOptions(true)
                .option("js.polyglot-builtin", "false")
                .option("js.graal-builtin", "false")
                .option("js.java-package-globals", "false").build();
    }

    @Override
    public void run() {
        isStarted = true;

        script.setSchedTime(new Date());
        script.setStatus(ScriptStatus.EXECUTING);
        dbService.saveScript(script);

        ScriptStatus scriptStatus = ScriptStatus.COMPLETED;
        String errorMessage = null;

        try {
            context.eval("js", script.getBody());
        } catch (PolyglotException ex) {
            if (ex.isGuestException()) {
                if (ex.isCancelled()) {
                    scriptStatus = ScriptStatus.INTERRUPTED;
                    errorMessage = "Execution was interrupted";
                } else {
                    scriptStatus = ScriptStatus.FAILED;
                    errorMessage = ex.getMessage();
                }
            }
        } catch (IllegalStateException ex) {
            scriptStatus = ScriptStatus.INTERRUPTED;
            errorMessage = "Execution was interrupted";
        } finally {
            isExecuted.set(true);

            int execTime = script.getExecTime();

            try {
                if (errorMessage != null) {
                    outputStream.write(errorMessage.getBytes());
                }
                outputStream.close();
            } catch (IOException ignored) {}

            script = dbService.findScript(script.getId());
            script.setStatus(scriptStatus);
            script.setExecTime(execTime);
            dbService.saveScript(script);

            if (scriptStatus != ScriptStatus.INTERRUPTED) {
                context.close();
            }
        }
    }

    public void stop() {
        if (isStarted && isExecuted.compareAndSet(false, true)) {
            controlledStream.block();
            context.close(true);
        }
    }

    public boolean isStarted() {
        return isStarted;
    }

    private OutputStream createOutputStream(Script script, OutputStream outputStream) {
        OutputStream stream = new DelayedOutputStream(
                new ScriptDbOutputStream(script.getId(), dbService), 200);
        if (outputStream != null) {
            stream = new OutputStreamDistributor(stream, outputStream);
        }
        return stream;
    }
}
