# Advanced Topics

Deep dive into TFPP's advanced features and architectural concepts.

## Architecture Overview

TFPP follows a modular architecture designed for flexibility and extensibility:

### Core Components

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   CLI Layer     │───▶│   Core Engine   │───▶│  Data Sources   │
│  (tfpp.cli)     │    │  (tfpp.core)    │    │  (tfpp.data)    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  Configuration  │    │  File Watching  │    │  Template Mode  │
│ (tfpp.config)   │    │   & Processing  │    │   Detection     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### Processing Pipeline

1. **Input Validation**: Verify file paths and arguments
2. **Template Detection**: Analyze file extensions and content
3. **Data Loading**: Load and merge data from multiple sources
4. **Context Building**: Create Thymeleaf context with merged data
5. **Template Processing**: Apply Thymeleaf engine with detected mode
6. **Output Generation**: Write processed content to destination
7. **Error Reporting**: Capture and report processing issues

## Performance Optimization

### Template Caching

TFPP automatically caches compiled templates for better performance:

```bash
# First run - template compilation
tfpp process template.html --data data.json  # ~100ms

# Subsequent runs - cached template
tfpp process template.html --data data.json  # ~20ms
```

### Parallel Processing

When processing directories, TFPP uses parallel processing:

```bash
# Process multiple templates concurrently
tfpp process-dir templates/ output/ --data config.yaml
```

**Configuration for parallel processing:**

```yaml
# tfpp.yml
processing:
  parallel: true
  max-threads: 4  # Defaults to CPU cores
  batch-size: 10  # Files per batch
```

### Memory Management

For large datasets, consider:

1. **Streaming data**: Process data in chunks rather than loading all at once
2. **Template fragmentation**: Break large templates into smaller fragments
3. **Memory monitoring**: Use `-Xmx` JVM flags to control memory usage

```bash
# Example with memory configuration
java -Xmx2g -jar tfpp.jar process-dir large-templates/ output/
```

## Advanced Data Handling

### Custom Data Sources

Extend TFPP to support custom data sources:

```kotlin
// Example: Database data source
class DatabaseDataSource(private val connection: Connection) : DataSource {
    override fun load(query: String): Map<String, Any> {
        // Implementation for loading data from database
        return executeQuery(query)
    }
}
```

### Data Transformation

Transform data before template processing:

```yaml
# tfpp.yml
data:
  transformations:
    - type: "date-format"
      fields: ["createdAt", "updatedAt"]
      format: "yyyy-MM-dd"
    
    - type: "currency-format"
      fields: ["price", "total"]
      currency: "USD"
```

### Complex Data Merging

Handle complex merging scenarios:

```bash
# Multiple data sources with precedence
tfpp process template.html \
  --data base-config.yaml \
  --data environment-config.yaml \
  --var environment=production \
  --var debug=false
```

**Merge strategy:**
1. `base-config.yaml` (lowest precedence)
2. `environment-config.yaml` 
3. CLI variables (highest precedence)

## Development Tools

### Watch Mode Advanced Features

```bash
# Watch with custom patterns
tfpp process-dir src/ dist/ --watch \
  --include "*.html,*.xml" \
  --exclude "*.tmp,*.backup"

# Watch with delay (debouncing)
tfpp process-dir src/ dist/ --watch --delay 500ms
```

### Debug Mode

Enable detailed debugging:

```bash
# Verbose output
tfpp process template.html --data data.json --verbose

# Debug mode with template analysis
tfpp process template.html --data data.json --debug
```

**Debug output includes:**
- Template parsing time
- Data loading time  
- Context building details
- Processing statistics
- Memory usage information

### Custom Extensions

Create custom Thymeleaf dialects:

```kotlin
// Example: Custom utility functions
@Component
class CustomDialect : AbstractDialect() {
    override fun getPrefix() = "tfpp"
    
    override fun getProcessors(dialectTemplateMode: TemplateMode): Set<IProcessor> {
        return setOf(
            CustomFormatProcessor(),
            CustomValidationProcessor()
        )
    }
}
```

## Integration Patterns

### CI/CD Integration

```yaml
# GitHub Actions example
name: Generate Documentation
on: [push, pull_request]

jobs:
  docs:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Setup Java
        uses: actions/setup-java@v2
        with:
          java-version: '21'
      
      - name: Generate Documentation
        run: |
          curl -fsSL https://raw.githubusercontent.com/kyungw00k/tfpp/main/install.sh | sudo bash
          tfpp process-dir docs/ output/ --data config.yaml
      
      - name: Deploy to GitHub Pages
        uses: peaceiris/actions-gh-pages@v3
        with:
          github_token: ${{ secrets.GITHUB_TOKEN }}
          publish_dir: ./output
```

### Build System Integration

#### Gradle Integration

```kotlin
// build.gradle.kts
tasks.register<Exec>("generateDocs") {
    dependsOn("jar")
    commandLine("tfpp", "process-dir", "docs/", "build/docs/", 
                "--data", "config.yaml")
}

tasks.named("build") {
    dependsOn("generateDocs")
}
```

#### Maven Integration

```xml
<!-- pom.xml -->
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>exec-maven-plugin</artifactId>
    <executions>
        <execution>
            <phase>generate-resources</phase>
            <goals>
                <goal>exec</goal>
            </goals>
            <configuration>
                <executable>tfpp</executable>
                <arguments>
                    <argument>process-dir</argument>
                    <argument>src/main/templates</argument>
                    <argument>target/generated-docs</argument>
                    <argument>--data</argument>
                    <argument>config.yaml</argument>
                </arguments>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Security Considerations

### Template Security

1. **Input Sanitization**: Always validate template inputs
2. **Path Traversal Prevention**: Restrict template file access
3. **Expression Evaluation**: Limit expression complexity
4. **Resource Access**: Control file system access

### Data Security

1. **Sensitive Data**: Never log sensitive information
2. **Data Validation**: Validate all input data
3. **Encryption**: Encrypt sensitive configuration files
4. **Access Control**: Implement proper file permissions

### Best Practices

```yaml
# Secure configuration example
security:
  template-access:
    allowed-paths: ["templates/", "fragments/"]
    denied-patterns: ["../*", "*.conf"]
  
  data-access:
    max-file-size: "10MB"
    allowed-extensions: [".json", ".yaml", ".yml"]
    
  processing:
    timeout: "30s"
    max-memory: "512MB"
```