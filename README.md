# TFPP (Thymeleaf File Preprocessor)

> A FMPP-inspired CLI tool powered by Thymeleaf for fast template processing and data binding experiments.

## ✅ Status: Ready for Use!

TFPP is a fully functional CLI template processor powered by Thymeleaf. All core features have been implemented and tested.

## 📋 Project Goals

- **Fast Data Binding Experiments**: Quickly test template rendering with various data sources
- **Thymeleaf Native Features**: Leverage Thymeleaf's unique capabilities like Natural Templates
- **Developer-Friendly**: Intuitive CLI interface with excellent error reporting
- **Multiple Template Modes**: Support HTML, TEXT, CSS, JavaScript processing

## 🚀 Quick Start

### Installation

#### Option 1: Quick Install (Recommended)

**System-wide installation:**
```bash
curl -fsSL https://raw.githubusercontent.com/kyungw00k/tfpp/main/install.sh | sudo bash
```

**Custom directory installation:**
```bash
curl -fsSL https://raw.githubusercontent.com/kyungw00k/tfpp/main/install.sh | bash -s ~/bin
```

#### Option 2: From Source

1. **Clone and build:**
   ```bash
   git clone https://github.com/kyungw00k/tfpp.git
   cd tfpp
   ./gradlew jar
   ```

2. **Install system-wide:**
   ```bash
   sudo ./install.sh
   ```

3. **Or install to custom directory:**
   ```bash
   ./install.sh ~/bin
   ```

4. **Or use directly:**
   ```bash
   ./bin/tfpp --help
   ```

### Basic Usage

```bash
# Process single template with variables
tfpp process template.html --var name="John" --var age=30

# Process with data file
tfpp process template.html --data data.json --output result.html

# Process entire directory
tfpp process-dir templates/ output/ --data site.yaml --verbose

# Watch for changes (development mode)
tfpp process-dir templates/ output/ --data site.yaml --watch
```

## 🎯 Features

### Core Functionality
- Single file and recursive directory processing
- Multiple data binding options (JSON/YAML files, direct input, key-value pairs)
- Automatic template mode detection (HTML, TEXT, CSS, JavaScript)
- Configuration system with Convention over Configuration

### Developer Tools
- Watch mode for automatic reprocessing
- User-friendly error reporting with precise location tracking
- Performance measurement and optimization
- Interactive mode for ambiguous situations

### Data Binding
- JSON/YAML file support
- Direct JSON input via CLI
- Key-value pair specification
- Multiple data source merging with conflict resolution

## 🛠 Technical Stack

- **Language**: Kotlin
- **Build Tool**: Gradle
- **CLI Framework**: Clikt
- **Template Engine**: Thymeleaf 3.1.x
- **Data Processing**: Jackson (JSON/YAML)
- **Documentation**: KDocs + GitHub Pages

## 📖 Planned CLI Interface

```bash
# Basic usage
tfpp <template-file> [options]
tfpp process [source] [options]

# Key options
--data, -d <file>          # Data file specification
--var, -v <key=value>      # Direct variable specification
--output, -o <path>        # Output path
--mode, -m <mode>          # Force template mode
--config, -c <file>        # Configuration file
--watch, -w                # Watch mode
--verbose                  # Verbose logging
```

## 📚 Documentation Plan

- **API Documentation**: Auto-generated KDocs
- **User Guide**: Comprehensive tutorials and examples
- **GitHub Pages**: Online documentation site
- **Example Collection**: Various use case demonstrations

## 🗂 Project Structure

```
tfpp/
├── cli/                    # CLI entry point and command processing
├── core/                   # Core template processing logic
├── data/                   # Data loaders and binding
├── config/                 # Configuration file processing
├── error/                  # Error handling system
├── utils/                  # Utility functions
└── docs/                   # Documentation and examples
```

## 📋 Development Progress

See [IMPLEMENTATION_CHECKLIST.md](IMPLEMENTATION_CHECKLIST.md) for detailed progress tracking.

## 🔧 Development

### Building from Source

```bash
git clone https://github.com/kyungw00k/tfpp.git
cd tfpp
./gradlew jar
```

### Running Tests

```bash
./gradlew test
```

### Local Development

```bash
# Use the wrapper script for development
./bin/tfpp --help

# Or run JAR directly  
java -jar app/build/libs/tfpp.jar --help
```

## 📄 References

- **FMPP Manual**: https://fmpp.sourceforge.net/manual.html
- **Thymeleaf Documentation**: https://www.thymeleaf.org/doc/tutorials/3.1/usingthymeleaf.html
- **Project Plan**: [PROJECT_PLAN.md](PROJECT_PLAN.md)

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request. For major changes, please open an issue first to discuss what you would like to change.

## 📜 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Inspired by [FMPP](https://fmpp.sourceforge.net/) (FreeMarker-based File PreProcessor)
- Powered by [Thymeleaf](https://www.thymeleaf.org/) template engine
- Built with [Kotlin](https://kotlinlang.org/) and [Gradle](https://gradle.org/)