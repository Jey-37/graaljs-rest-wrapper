package com.example.jsrest.service;

import java.util.concurrent.Future;

public record ThreadManagePair(ScriptThread thread, Future<?> future) {
}
