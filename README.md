# GraalJS REST wrapper

The simple REST wrapper for GraalJS that allows running scripts in synchronous and asynchronous modes and stopping them before completion.

## Requirements

1. [GraalVM](https://graalvm.org/)
2. [GraalJS module](https://github.com/oracle/graaljs)

## Running

### Windows

```shell
cd "path/to/cloned/repository"
set "JAVA_HOME=path/to/graalvm"
mvnw package
"%JAVA_HOME%/bin/java.exe" -Dpolyglot.image-build-time.PreinitializeContexts=js -jar target/graaljs-rest-wrapper-0.0.1-SNAPSHOT.jar
```

### Linux

```bash
cd "path/to/cloned/repository"
export JAVA_HOME="/path/to/graalvm"
mvnw package
$JAVA_HOME/bin/java -Dpolyglot.image-build-time.PreinitializeContexts=js -jar target/graaljs-rest-wrapper-0.0.1-SNAPSHOT.jar
```
<br/>
You can also add --running-threads-number argument that points out the number of threads that can process scripts at once (default value is 3).

Example (Windows):

```shell
"%JAVA_HOME%/bin/java.exe" -Dpolyglot.image-build-time.PreinitializeContexts=js -jar target/graaljs-rest-wrapper-0.0.1-SNAPSHOT.jar --running-threads-number=4
```

## Usage

Send a script for execution in async mode (send and forget)

```shell
curl -X POST localhost:8080/scripts -H "Content-Type: text/plain" -d @script.txt
```

Send a script for execution in sync (blocking) mode (realtime script's output streaming).<br/>
It should be noted that response streaming doesn't work correctly in Postman.

```shell
curl -X POST localhost:8080/scripts?blocking -H "Content-Type: text/plain" -d @script.txt
```

Stop running or queuing script

```shell
curl -X POST localhost:8080/scripts/{id}
```

Get all scripts (request params are optional).<br/>
Available status values: queued, executing, completed, failed, interrupted.<br/>
Available orderBy values: id, schedTime, pubTime.

```shell
curl localhost:8080/scripts?status=interrupted&orderBy=schedTime
```

Get all information about a script (including script's output).

```shell
curl localhost:8080/scripts/{id}
```

Remove a finished script.

```shell
curl -X DELETE localhost:8080/scripts/{id}
```
