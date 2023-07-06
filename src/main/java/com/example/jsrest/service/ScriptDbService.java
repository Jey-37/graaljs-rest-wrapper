package com.example.jsrest.service;

import com.example.jsrest.model.Script;
import com.example.jsrest.repo.ScriptRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScriptDbService
{
    private final ScriptRepository repo;

    public ScriptDbService(ScriptRepository repo) {
        this.repo = repo;
    }

    public Script findScript(long id) {
        return repo.findById(id).orElse(null);
    }

    public Iterable<Script> findScripts(String status, String orderBy) {
        Script.ScriptStatus scriptStatus;
        try {
            scriptStatus = status != null ? Script.ScriptStatus.valueOf(status.toUpperCase()) : null;
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Illegal status parameter value");
        }
        if (orderBy != null) {
            if (!List.of("id", "schedTime", "pubTime").contains(orderBy))
                throw new IllegalArgumentException("Illegal orderBy parameter value");
            if (scriptStatus != null)
                return repo.findByStatus(scriptStatus, Sort.by(orderBy).descending());
            return repo.findAll(Sort.by(orderBy).descending());
        }
        if (scriptStatus != null)
            return repo.findByStatus(scriptStatus, null);

        return repo.findAll();
    }

    public boolean removeFinishedScript(long id) {
        var script = repo.findById(id);

        if (script.isPresent()) {
            if (script.get().getStatus().isFinished()) {
                repo.deleteById(script.get().getId());
                return true;
            }
        }

        return false;
    }

    public Script saveScript(Script script) {
        return repo.save(script);
    }

    @Transactional
    public void appendOutput(long id, String output) {
        repo.appendOutput(id, output);
    }
}
