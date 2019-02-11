# NexusVault CLI
Version 0.1.2

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
If no appropriate Java version can't be found, the launcher will open a Java download page.

#### Why is the release not packed with Java?
GitHub has a strict limit of files exceeding 100 MB in size
Moreover the current Java RE is roughly ~200 MB in size, while this application is ~8MB, not only would it unnecessary bloat the size of the release up, it would also make the download more time-consuming. While the application may be updated frequently to include new features, the JRE will probably not change.

### Run the application
Before anything can be done, the application needs to know which archives should be read, to do so either start the application with the flag `-a "path to folder"` or write `archive "path to folder"` after starting it. The application will immediately search for any archives there. It is also possible to load a specific archive by passing the direct file path to the application.
For example:
* `-a "C:\Games\WildStar"`
* `archive "C:\Games\Wildstar"`
* `-a "C:\Games\WildStar\Patch\ClientData.archive" "C:\Games\WildStar\Patch\ClientDataEN.archive"`

## Dependencies

* JDK 1.8
* [NexusVault library](https://github.com/MarbleBag/NexusVault/tree/java)


## Built With

* [Maven](https://maven.apache.org/) - Dependency Management

## Versioning

This project is versioned with [SemVer](http://semver.org/)
and stores a list of major changes in its [changelog](CHANGELOG.md), including upcoming changes

## License

This project is licensed under the GNU AGPLv3 License - see the [LICENSE.md](LICENSE.md) file for details

