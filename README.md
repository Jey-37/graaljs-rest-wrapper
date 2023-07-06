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
