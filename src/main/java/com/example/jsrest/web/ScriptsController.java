package com.example.jsrest.web;

import com.example.jsrest.dto.ScriptDto;
import com.example.jsrest.model.Script;
import com.example.jsrest.repo.ScriptRepository;
import com.example.jsrest.service.ScriptRunner;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/scripts")
public class ScriptsController
{
    private final ModelMapper modelMapper = new ModelMapper();
    private final ScriptRepository repo;
    private final ScriptRunner scriptRunner;

    public ScriptsController(ScriptRepository repo, ScriptRunner scriptRunner) {
        this.repo = repo;
        this.scriptRunner = scriptRunner;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public long runScript(@RequestBody String scriptText) {
        return scriptRunner.runScript(scriptText);
    }

    @GetMapping
    public Iterable<ScriptDto> getScripts(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String orderBy) {
        Collection<Script> scripts = new ArrayList<>();
        findScripts(status, orderBy).forEach(scripts::add);
        return scripts.stream()
                .map(script -> modelMapper.map(script, ScriptDto.class))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public Optional<Script> getScriptInfo(@PathVariable Long id) {
        var script = repo.findById(id);
        if (script.isPresent()) {
            var scriptOutput = scriptRunner.getScriptOutput(id);
            if (scriptOutput != null) {
                script.get().setOutput(scriptOutput);
            }
        }
        return script;
    }

    @PostMapping("/{id}")
    public void stopScript(@PathVariable Long id) {
        scriptRunner.stopScript(id);
    }

    @DeleteMapping("/{id}")
    public void removeScript(@PathVariable Long id) {
        var script = repo.findById(id);

        if (script.isPresent())
            if (script.get().getStatus().isFinished())
                repo.delete(script.get());
    }

    private Iterable<Script> findScripts(String status, String orderBy) {
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
}
