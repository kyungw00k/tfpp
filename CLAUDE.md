# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

TFPP (Thymeleaf File Preprocessor) is a CLI tool inspired by FMPP that uses Thymeleaf as its template engine. It's written in Kotlin and built with Gradle. The tool processes templates with data binding, supporting both single file processing and directory recursion with watch mode.

## Architecture Overview

The codebase follows a modular architecture with clear separation of concerns:

1. **CLI Layer** (`app/src/main/kotlin/tfpp/cli`) - Handles command-line parsing and user interaction using Clikt
2. **Core Engine** (`app/src/main/kotlin/tfpp/core`) - Contains the template processing logic and file handling
3. **Data Loading** (`app/src/main/kotlin/tfpp/data`) - Manages data loading from JSON/YAML files and key-value pairs
4. **Configuration** (`app/src/main/kotlin/tfpp/config`) - Handles configuration loading (if implemented)

Key classes:
- `TfppCli` - Main CLI application with subcommands
- `ProcessCommand` - Handles single template processing
- `ProcessDirCommand` - Handles directory processing with recursion
- `TemplateEngine` - Wrapper around Thymeleaf engine
- `FileProcessor` - Handles file operations and directory traversal
- `DataLoader` - Loads and parses data from various sources
- `FileWatcher` - Implements file watching for development mode

## Common Development Tasks

### Building the Project
```bash
./gradlew jar              # Build the fat JAR with all dependencies
./gradlew test             # Run tests
./gradlew clean            # Clean build artifacts
```

### Running the Application
```bash
# Using the wrapper script (development)
./bin/tfpp --help

# Using the built JAR directly
java -jar app/build/libs/tfpp.jar --help

# Or after installing system-wide
tfpp --help
```

### Testing
```bash
./gradlew test                           # Run all tests
./gradlew test --tests "*TestClassName*" # Run specific test class
```

### Documentation Generation
```bash
./gradlew mkdocsBuild                    # Build MkDocs documentation
./gradlew serveDocs                      # Serve documentation locally
```

## Key Components and Their Responsibilities

### CLI Layer (App.kt)
- Defines the main CLI interface with subcommands: `process` and `process-dir`
- Handles argument parsing, validation, and error reporting
- Coordinates between core components based on user input

### Template Engine (TemplateEngine.kt)
- Wraps Thymeleaf's TemplateEngine
- Handles template mode detection (HTML, XML, TEXT, CSS, JAVASCRIPT)
- Processes templates with context data

### File Processor (FileProcessor.kt)
- Handles single file and directory processing
- Manages file copying for non-template assets
- Reports processing results and errors

### Data Loader (DataLoader.kt)
- Loads data from JSON and YAML files
- Parses key-value pair arguments
- Merges data from multiple sources

### File Watcher (FileWatcher.kt)
- Monitors file changes using directory-watcher library
- Triggers reprocessing when templates or data files change
- Handles create, modify, and delete events

## File Extensions and Template Detection

Default template extensions: html, htm, xml, txt, text, css, js, json, yaml, yml, md, markdown, sql, properties

Template mode is detected based on:
1. File extension (primary method)
2. Content analysis for ambiguous cases (fallback)

## Development Workflow

1. Make changes to Kotlin source files in `app/src/main/kotlin/`
2. Build with `./gradlew jar`
3. Test changes with `./bin/tfpp` or directly with `java -jar`
4. Run tests with `./gradlew test`
5. Generate documentation with `./gradlew mkdocsBuild`

## Important Implementation Details

- The application uses Kotlin coroutines for asynchronous operations (particularly in FileWatcher)
- Thymeleaf caching is disabled for development (`isCacheable = false`)
- Error handling is centralized with user-friendly error messages
- File paths are handled with proper relative path resolution
- The fat JAR includes all dependencies for standalone execution

## Dependencies

Key dependencies managed in `gradle/libs.versions.toml`:
- Clikt for CLI parsing
- Thymeleaf for template processing
- Jackson for JSON/YAML processing
- Directory-watcher for file monitoring
- Kotlin coroutines for async operations
- MkDocs with Material theme for documentation