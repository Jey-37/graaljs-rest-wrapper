package com.example.jsrest.service;

import com.example.jsrest.model.Script;
import org.graalvm.polyglot.Context;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

@Service
public class ScriptRunner
{
    private final ExecutorService executorService;
    private final ConcurrentMap<Long, ThreadManagePair> processedTasks = new ConcurrentHashMap<>();
    private final ScriptDbService dbService;

    private record ThreadManagePair(ScriptTask thread, Future<?> future) { }

    public ScriptRunner(@Value("${script-runner.threads-number:3}") int threadsNumber,
                        ScriptDbService dbService) {
        executorService = Executors.newFixedThreadPool(threadsNumber);
        this.dbService = dbService;

        try (Context context = Context.create("js")) {
            context.initialize("js");
        }
    }

    public long runScript(String scriptText) {
        Script script = new Script();
        script.setBody(scriptText);
        dbService.saveScript(script);

        ScriptTask scriptTask = new ScriptTask(script, dbService, this);
        var fut = executorService.submit(scriptTask);
        processedTasks.put(script.getId(), new ThreadManagePair(scriptTask, fut));

        return script.getId();
    }

    public boolean stopScript(long id) {
        var taskMan = processedTasks.get(id);
        if (taskMan != null) {
            if (taskMan.thread().isRunning()) {
                taskMan.thread().closeContext();
            } else {
                taskMan.future().cancel(false);
                processedTasks.remove(id);
                var script = dbService.findScript(id).get();
                script.setStatus(Script.ScriptStatus.INTERRUPTED);
                dbService.saveScript(script);
            }
            return true;
        }
        return false;
    }

    public void updateScriptData(Script script) {
        var manMap = processedTasks.get(script.getId());
        if (manMap != null && manMap.thread().isRunning()) {
            script.setOutput(manMap.thread().getOutput());
            script.setExecTime(manMap.thread().getExecutionTime());
        }
    }

    public void removeTask(long id) {
        processedTasks.remove(id);
    }
}
