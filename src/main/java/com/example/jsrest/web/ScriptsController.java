package com.example.jsrest.web;

import com.example.jsrest.model.Script;
import com.example.jsrest.service.ScriptDbService;
import com.example.jsrest.service.ScriptRunner;
import com.example.jsrest.streams.DelayedOutputStream;
import com.example.jsrest.streams.ResponseEmitterOutputStream;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.IOException;


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
    public ResponseEntity<ResponseBodyEmitter> runScript(
            @RequestBody String scriptText,
            @RequestParam(required = false) String blocking) throws IOException {

        ResponseBodyEmitter emitter = new ResponseBodyEmitter();

        if (blocking != null && !blocking.equalsIgnoreCase("false")) {
            long scriptId = scriptRunner.runScript(scriptText,
                    new DelayedOutputStream(new ResponseEmitterOutputStream(emitter), 200));
            emitter.send(String.format("Script ID: %d\n", scriptId));

            return ResponseEntity.ok(emitter);
        } else {
            long scriptId = scriptRunner.runScript(scriptText);
            emitter.send(scriptId);
            emitter.complete();

            return ResponseEntity.accepted().body(emitter);
        }
    }

    @GetMapping
    @JsonView(Script.Views.ShortInfo.class)
    public Iterable<Script> getScripts(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String orderBy) {
        try {
            return scriptDbService.findScripts(status, orderBy);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Script> getScriptInfo(@PathVariable long id) {
        return ResponseEntity.ofNullable(scriptDbService.findScript(id));
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
