package com.example.jsrest.web;

import com.example.jsrest.dto.ScriptDto;
import com.example.jsrest.model.Script;
import com.example.jsrest.service.ScriptDbService;
import com.example.jsrest.service.ScriptRunner;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/scripts")
public class ScriptsController
{
    private final ModelMapper modelMapper = new ModelMapper();
    private final ScriptDbService scriptDbService;
    private final ScriptRunner scriptRunner;

    public ScriptsController(ScriptDbService scriptDbService, ScriptRunner scriptRunner) {
        this.scriptDbService = scriptDbService;
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
        List<Script> scripts = new ArrayList<>();
        scriptDbService.findScripts(status, orderBy).forEach(scripts::add);
        return scripts.stream()
                .map(script -> modelMapper.map(script, ScriptDto.class))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public Optional<Script> getScriptInfo(@PathVariable long id) {
        var script = scriptDbService.findScript(id);
        script.ifPresent(scriptRunner::updateScriptData);
        return script;
    }

    @PostMapping("/{id}")
    public void stopScript(@PathVariable long id) {
        scriptRunner.stopScript(id);
    }

    @DeleteMapping("/{id}")
    public void removeScript(@PathVariable long id) {
        scriptDbService.removeFinishedScript(id);
    }
}
