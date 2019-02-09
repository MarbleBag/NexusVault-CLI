# Changelog
All notable changes to this project will be documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com)

## [Unreleased] 

## [0.1.1] - 2019-02-09
### Added
- Show command -> show. For now only shows the results of the last 'search' command
- Debug command. Allows to toggle the debug mode of the app on/off. Debug mode will output more informations to the console
- m3 export 'gltf' mode will now also export textures

### Changed 
- m3 export 'debug' mode will now bundle its output into a new folder, named after the m3, on the same folder level as the m3

### Fixed 
- Changing a value with export-m3-type will now correctly show the old value
- tex export didn't use the correct reader. Will no longer produce empty folders

### Deprecated 
- ModelSystem

## [0.1.0] - 2019-02-05
