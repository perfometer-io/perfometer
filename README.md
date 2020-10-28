# perfometer
Awesome stress testing lib in Kotlin

## Usage

### Build executable jar

```
./gradlew clean shadowJar
```

### Run scenario from file

```
cd build/libs
java -jar perfometer-all.jar /path/to/scenario.perf.kts
```

You could find example scenarios here in the [examples](examples) folder.
