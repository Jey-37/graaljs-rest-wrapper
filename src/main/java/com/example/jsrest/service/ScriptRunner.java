package com.example.jsrest.service;

import com.example.jsrest.model.Script;
import com.example.jsrest.repo.ScriptRepository;
import org.graalvm.polyglot.Context;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ScriptRunner
{
    private final ExecutorService executorService;
    private final ConcurrentMap<Long, ThreadManagePair> processedThreads = new ConcurrentHashMap<>();

    private final ScriptRepository repo;

    public ScriptRunner(ScriptRepository repo,
                        @Value("${script-runner.threads-number:3}") int threadsNumber) {
        this.repo = repo;
        executorService = Executors.newFixedThreadPool(threadsNumber);
        try (Context context = Context.create("js")) {
            context.initialize("js");
        }
    }

    public long runScript(String scriptText) {
        Script script = new Script();
        script.setBody(scriptText);
        repo.save(script);

        ScriptThread runThread = new ScriptThread(script, repo, processedThreads);
        var fut = executorService.submit(runThread);
        processedThreads.put(script.getId(), new ThreadManagePair(runThread, fut));

        return script.getId();
    }

    public void stopScript(long id) {
        var threadMan = processedThreads.get(id);
        if (threadMan != null) {
            if (threadMan.thread().isRunning()) {
                threadMan.thread().closeContext();
            } else {
                threadMan.future().cancel(false);
                processedThreads.remove(id);
                var script = repo.findById(id).get();
                script.setStatus(Script.ScriptStatus.INTERRUPTED);
                repo.save(script);
            }
        }
    }

    public String getScriptOutput(long id) {
        var manMap = processedThreads.get(id);
        if (manMap != null && manMap.thread().isRunning()) {
            return manMap.thread().getOutput();
        }
        return null;
    }
}
