# NexusVault CLI
Version 0.1.4 (Beta)

A CLI application which incorporates the [NexusVault library](https://github.com/MarbleBag/NexusVault/tree/java)
In its current form it mainly provides a way to traverse the content of .archive files and export its content, either in its original form or converted to a more known format.

### Getting started

Check out the [latest release](https://github.com/MarbleBag/NexusVault-CLI/releases/latest).
The release comes prepacked with all needed dependencies (except Java) and a launch4j launcher, which supports to run the application without a Java installation, as long as it can find a valid JRE besides it. 

```Bash
├── NexusVaultCLI
   ├── jre  <---
   ├── libs
   ├── nexusvault-cli.jar
   └── NexusvaultCLI.exe
```

Is no local Java version provide, the launcher will look up Java installations that are available on the system.

### Run the application
Before anything can be done, the application needs to know which archives should be read, to do so either start the application with the flag `-a "path to folder"` or write `archive "path to folder"` after starting it. The application will immediately search for any archives there. It is also possible to load a specific archive by passing the direct file path to the application.
For example:
* `-a "C:\Games\WildStar"`
* `archive "C:\Games\Wildstar"`
* `-a "C:\Games\WildStar\Patch\ClientData.archive" "C:\Games\WildStar\Patch\ClientDataEN.archive"`

## Dependencies

* JDK 11
* [NexusVault library](https://github.com/MarbleBag/NexusVault/tree/java)

### Java module dependencies
The application can also run on a JRE 11 which contains the listed modules:

- java.desktop
- java.logging
- java.management

JLink can be used to create a minimal JRE with all requirements:
* jlink --no-header-files --no-man-pages --compress=2 --strip-debug --add-modules java.desktop,java.logging,java.management

## Built With

* [Maven](https://maven.apache.org/) - Dependency Management

## Versioning

This project is versioned with [SemVer](http://semver.org/)
and stores a list of major changes in its [changelog](CHANGELOG.md), including upcoming changes

## License

This project is licensed under the GNU AGPLv3 License - see the [LICENSE.md](LICENSE.md) file for details

