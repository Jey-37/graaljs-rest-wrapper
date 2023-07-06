package com.example.jsrest.service;

import com.example.jsrest.model.Script;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.*;

@Service
public class ScriptRunner
{
    private final ExecutorService executorService;

    private final ConcurrentMap<Long, ScriptRunPair> processedTasks = new ConcurrentHashMap<>();

    private final ScriptDbService dbService;

    record ScriptRunPair(ScriptTask task, CompletableFuture<?> future) {}

    public ScriptRunner(@Value("${running-threads-number:3}") int threadsNumber,
                        ScriptDbService dbService) {
        System.out.println(threadsNumber);
        executorService = Executors.newFixedThreadPool(threadsNumber);
        this.dbService = dbService;
    }

    public long runScript(String scriptBody) {
        return runScript(scriptBody, null);
    }

    public long runScript(String scriptBody, OutputStream outputStream) {
        Script script = new Script(scriptBody);
        dbService.saveScript(script);

        var task = new ScriptTask(script, dbService, outputStream);

        var future = CompletableFuture.runAsync(task, executorService);

        future.thenRun(() -> processedTasks.remove(script.getId()));

        future.whenComplete((res, ex) -> {
            if (future.isCancelled()) {
                Script updatedScript = dbService.findScript(script.getId());
                updatedScript.setStatus(Script.ScriptStatus.INTERRUPTED);
                updatedScript.setOutput("Execution was cancelled");
                dbService.saveScript(updatedScript);

                if (outputStream != null) {
                    try {
                        outputStream.write("Execution was cancelled".getBytes());
                        outputStream.close();
                    } catch (IOException ignored) {}
                }
            }
        });

        processedTasks.put(script.getId(), new ScriptRunPair(task, future));

        return script.getId();
    }

    public boolean stopScript(long id) {
        var scriptRunPair = processedTasks.remove(id);
        if (scriptRunPair != null) {
            if (scriptRunPair.task.isStarted()) {
                scriptRunPair.task.stop();
            } else {
                scriptRunPair.future.cancel(false);
            }
            return true;
        }
        return false;
    }
}
