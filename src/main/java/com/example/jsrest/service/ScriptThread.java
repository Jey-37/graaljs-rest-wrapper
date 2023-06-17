package com.example.jsrest.service;

import com.example.jsrest.model.Script;
import com.example.jsrest.model.Script.ScriptStatus;
import com.example.jsrest.repo.ScriptRepository;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotException;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.concurrent.ConcurrentMap;

public class ScriptThread implements Runnable
{
    private volatile boolean isRunning = false;
    private final ScriptRepository repo;
    private final Script script;
    private final Context context;
    private final OutputStream outputStream = new ByteArrayOutputStream();
    private final ConcurrentMap<Long, ThreadManagePair> processedThreads;

    public ScriptThread(Script script, ScriptRepository repo, ConcurrentMap<Long, ThreadManagePair> processedThreads) {
        this.script = script;
        this.repo = repo;
        this.processedThreads = processedThreads;

        context = Context.newBuilder("js")
                .out(outputStream).allowHostAccess(HostAccess.NONE)
                .allowExperimentalOptions(true)
                .option("js.polyglot-builtin", "false")
                .option("js.graal-builtin", "false")
                .option("js.java-package-globals", "false").build();
    }

    @Override
    public void run() {
        try {
            isRunning = true;
            script.setSchedTime(new Date());
            script.setStatus(ScriptStatus.EXECUTING);
            repo.save(script);
            context.eval("js", script.getBody());
            script.setStatus(ScriptStatus.COMPLETED);
        } catch (PolyglotException ex) {
            if (ex.isGuestException()) {
                if (ex.isCancelled()) {
                    script.setStatus(ScriptStatus.INTERRUPTED);
                } else {
                    script.setStatus(ScriptStatus.FAILED);
                    script.setErrors(ex.getMessage());
                }
            }
        } finally {
            int execTime = (int)(new Date().getTime()-script.getSchedTime().getTime());
            script.setExecTime(execTime);
            script.setOutput(outputStream.toString());
            repo.save(script);
            context.close();
            processedThreads.remove(script.getId());
        }
    }

    public void closeContext() {
        context.close(true);
    }

    public String getOutput() {
        return outputStream.toString();
    }

    public boolean isRunning() {
        return isRunning;
    }
}
