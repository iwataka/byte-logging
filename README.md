# byte-logging

Javaagent program to log setter method call.

## Usage

```
gradle shadowJar
java -javaagent:/path/to/byte-logging-1.0-SNAPSHOT-all.jar[=classNamePattern] MainClassName
```
