# TFPP - Thymeleaf File Preprocessor

A powerful CLI tool for processing templates using Thymeleaf engine with data binding support.

## What is TFPP?

TFPP (Thymeleaf File Preprocessor) is a command-line tool inspired by FMPP that uses Thymeleaf as its template engine. It's designed to process templates with data binding, supporting both single file processing and directory recursion with watch mode for development.

## Key Features

- **🚀 Fast Template Processing**: Powered by Thymeleaf engine
- **📊 Multiple Data Formats**: JSON, YAML, and CLI variables
- **📁 Directory Processing**: Recursive template processing with file watching
- **🔄 Watch Mode**: Auto-reload during development
- **🎯 Smart Detection**: Automatic template mode detection
- **⚡ High Performance**: Template caching and parallel processing

## Quick Start

### Installation

```bash
curl -fsSL https://raw.githubusercontent.com/kyungw00k/tfpp/main/install.sh | sudo bash
```

### Basic Usage

```bash
# Process a single template
tfpp process template.html --data data.json --output result.html

# Process a directory with watch mode
tfpp process-dir templates/ output/ --data config.yaml --watch
```

## Template Example

**template.html:**
```html
<!DOCTYPE html>
<html>
<head>
    <title>[[${siteName}]]</title>
</head>
<body>
    <h1>Welcome, [[${user.name}]]!</h1>
    <ul>
        <li th:each="item : ${items}" th:text="${item.name}">Item</li>
    </ul>
</body>
</html>
```

**data.json:**
```json
{
  "siteName": "My Website",
  "user": { "name": "John" },
  "items": [
    { "name": "First Item" },
    { "name": "Second Item" }
  ]
}
```

## Next Steps

- [Getting Started Guide](getting-started.md) - Detailed installation and setup
- [Template Syntax](template-guide.md) - Learn Thymeleaf template syntax
- [Advanced Topics](advanced-topics.md) - Performance, architecture, and CI/CD