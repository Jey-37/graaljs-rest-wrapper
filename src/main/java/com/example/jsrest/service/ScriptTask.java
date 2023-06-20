package com.example.jsrest.service;

import com.example.jsrest.model.Script;
import com.example.jsrest.model.Script.ScriptStatus;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotException;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Date;

public class ScriptTask implements Runnable
{
    private volatile boolean isRunning = false;
    private final ScriptDbService dbService;
    private final ScriptRunner scriptRunner;
    private final Script script;
    private Context context;
    private final OutputStream outputStream = new ByteArrayOutputStream();

    public ScriptTask(Script script, ScriptDbService dbService,
                      ScriptRunner scriptRunner) {
        this.script = script;
        this.dbService = dbService;
        this.scriptRunner = scriptRunner;
    }

    @Override
    public void run() {
        try(Context context = Context.newBuilder("js")
                .out(outputStream).allowHostAccess(HostAccess.NONE)
                .allowExperimentalOptions(true)
                .option("js.polyglot-builtin", "false")
                .option("js.graal-builtin", "false")
                .option("js.java-package-globals", "false").build()) {
            this.context = context;
            isRunning = true;
            script.setSchedTime(new Date());
            script.setStatus(ScriptStatus.EXECUTING);
            dbService.saveScript(script);
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
            script.setExecTime(getExecutionTime());
            script.setOutput(getOutput());
            dbService.saveScript(script);
            scriptRunner.removeTask(script.getId());
        }
    }

    public void closeContext() {
        if (isRunning)
            context.close(true);
    }

    public String getOutput() {
        return outputStream.toString();
    }

    public int getExecutionTime() {
        return (int)(new Date().getTime()-script.getSchedTime().getTime());
    }

    public boolean isRunning() {
        return isRunning;
    }
}
