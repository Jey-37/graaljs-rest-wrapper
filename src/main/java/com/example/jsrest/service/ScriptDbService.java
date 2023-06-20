package com.example.jsrest.service;

import com.example.jsrest.model.Script;
import com.example.jsrest.repo.ScriptRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class ScriptDbService
{
    private final ScriptRepository repo;

    public ScriptDbService(ScriptRepository repo) {
        this.repo = repo;
    }

    public Optional<Script> findScript(long id) {
        return repo.findById(id);
    }

    public Iterable<Script> findScripts(String status, String orderBy) {
        try {
            var scriptStatus = status != null ? Script.ScriptStatus.valueOf(status.toUpperCase()) : null;
            if (orderBy != null) {
                if (!List.of("id", "schedTime").contains(orderBy))
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "The only possible values for orderBy parameter are id and schedTime");
                if (scriptStatus != null)
                    return repo.findByStatus(scriptStatus, Sort.by(orderBy).descending());
                return repo.findAll(Sort.by(orderBy).descending());
            }
            if (scriptStatus != null)
                return repo.findByStatus(scriptStatus, null);

            return repo.findAll();
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Illegal status parameter value");
        }
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
}
