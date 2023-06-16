package com.example.jsrest.service;

import com.example.jsrest.model.Script;
import com.example.jsrest.repo.ScriptRepository;
import org.graalvm.polyglot.Context;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ScriptRunner
{
    private final ExecutorService executorService = Executors.newFixedThreadPool(3);
    private final ConcurrentMap<Long, ThreadManagePair> processedThreads = new ConcurrentHashMap<>();

    private final ScriptRepository repo;

    public ScriptRunner(ScriptRepository repo) {
        this.repo = repo;
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
        if (processedThreads.containsKey(id)) {
            ThreadManagePair threadMan = processedThreads.get(id);
            if (threadMan.thread().isRunning()) {
                threadMan.thread().closeContext();
            } else {
                processedThreads.remove(id);
                threadMan.future().cancel(false);
                var script = repo.findById(id).get();
                script.setStatus(Script.ScriptStatus.INTERRUPTED);
                repo.save(script);
            }
        }
    }

    public String getScriptOutput(long id) {
        if (processedThreads.containsKey(id)) {
            return processedThreads.get(id).thread().getOutput();
        }
        return null;
    }
}
