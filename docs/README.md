# NexusVault CLI
Version 0.2.4

A CLI application which incorporates the [NexusVault library](https://github.com/MarbleBag/NexusVault)
In its current form it mainly provides a way to traverse the content of .archive files and export its content, either in its original form or converted to a more known format.



### Getting started

Check out the [latest release](https://github.com/MarbleBag/NexusVault-CLI/releases/latest).
The release comes prepacked ([7zip](https://www.7-zip.org/)) with all needed dependencies (except Java) and a launch4j launcher, which supports to run the application without a Java installation, as long as it can find a valid JRE beside it.

A Windows compatible Java version can be downloaded [here](https://github.com/MarbleBag/NexusVault-CLI/releases/tag/v0.1.5.beta), packed as JRE_11_small_WIN64.zip

```Bash
├── NexusVaultCLI
   ├── jre  <---
   ├── libs
   ├── nexusvault-cli.jar
   └── NexusvaultCLI.exe
```

Is no local Java version provide, the launcher will look up Java installations that are available on the system.
If no appropriate Java version can't be found, the launcher will open a Java download page.

### Run the application
Before anything can be done, the application needs to know which archives should be read, to do so either start the application with the flag `-a "path to folder"`, either via shortcut or batch/shell script, or write `archive "path to folder"` after starting it.

The application will immediately search for any archives there. It is also possible to load a specific archive by passing the direct file path to the application.

Console examples:
* `-a "C:\Games\WildStar"`
* `archive "C:\Games\Wildstar"`
* `-a "C:\Games\WildStar\Patch\ClientData.archive" "C:\Games\WildStar\Patch\ClientDataEN.archive"`

### How to find something to export
The export mechanism works in 2-steps.
* Step 1 is identifying what to export.

To do so use the build-in `search` command, which relies on Java's [regular expression pattern](https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html)
The archive is similar to a folder, which contains other folders and files. The expression for the search command looks at the full path for each file and determines if the path fulfills the expression or not.

As an example, the model for the female aurin is located under `Art\Character\Aurin\Female\aurin_f.m3`.
To find exactly this file, and no other, a possible `search`command could be `search art\\character\\aurin\\female\\aurin_f.m3` The search is case-insensitive and note the `\\`instead of `\` as folder separator.
Another possible command is `search \\aurin_f.m3`, this works because there is no other file with the name `aurin_f.m3`.
Whereas `search aurin_f.m3` will find over 1000 models, which all end in `aurin_f.m3`. The difference is, that `\\` in front of the file name tells the search command to look for files that start with the given name.

To check the results either take a look into the `report\search_result.txt` that is created by the tool or use `show search`,  though, depending on the size of the console this may be confusing.

* Step 2 export all the things

This is as simple as typing `export`
The tool will now export all files that were found with the last `search` or, in case no search was done, it can find in `report\search_result.txt`

## Dependencies

* JDK 11
* [NexusVault library](https://github.com/MarbleBag/NexusVault/tree/java)

### Java module dependencies
The application runs on a JRE 11 which contains the listed modules:

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
