package com.example.jsrest.web;

import com.example.jsrest.model.Script;
import com.example.jsrest.repo.ScriptRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/scripts")
public class ScriptsController
{
    private final ScriptRepository repo;

    public ScriptsController(ScriptRepository repo) {
        this.repo = repo;
    }

    @PostMapping
    public long runScript(@RequestBody String scriptText) {
        Script script = new Script();
        script.setBody(scriptText);
        repo.save(script);

        return script.getId();
    }

    @GetMapping
    public Iterable<Script> getScripts(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String orderBy) {
        if (orderBy != null) {
            if (!List.of("id", "schedTime").contains(orderBy))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "The only possible values for orderBy parameter are id and schedTime");
            if (status != null)
                return repo.findByStatus(status, Sort.by(orderBy).descending());
            return repo.findAll(Sort.by(orderBy).descending());
        }
        if (status != null)
            return repo.findByStatus(status, null);

        return repo.findAll();
    }

    @GetMapping("/{id}")
    public Optional<Script> getScriptInfo(@PathVariable Long id) {
        return repo.findById(id);
    }

    @PostMapping("/{id}")
    public void stopScript(@PathVariable Long id) {

    }

    @DeleteMapping("/{id}")
    public void removeScript(@PathVariable Long id) {
        Script script = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (List.of("completed", "failed", "interrupted").contains(script.getStatus()))
            repo.delete(script);
    }
}
