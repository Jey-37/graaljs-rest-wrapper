package com.example.jsrest.web;

import com.example.jsrest.model.Script;
import com.example.jsrest.service.ScriptDbService;
import com.example.jsrest.service.ScriptRunner;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/scripts")
public class ScriptsController
{
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
    @JsonView(Script.Views.ShortInfo.class)
    public Iterable<Script> getScripts(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String orderBy) {
        return scriptDbService.findScripts(status, orderBy);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Script> getScriptInfo(@PathVariable long id) {
        var script = scriptDbService.findScript(id);
        if (script.isPresent()) {
            scriptRunner.updateScriptData(script.get());
            return new ResponseEntity<>(script.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/{id}")
    public void stopScript(@PathVariable long id) {
        scriptRunner.stopScript(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeScript(@PathVariable long id) {
        scriptDbService.removeFinishedScript(id);
    }
}
