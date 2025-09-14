# Getting Started

This guide will help you get up and running with TFPP quickly.

## Prerequisites

Before you start, make sure you have:

- **Java 21+** installed on your system
- Basic familiarity with **command line tools**
- Understanding of **template concepts** (helpful but not required)

## Installation Options

### Quick Installation

The fastest way to get TFPP running:

```bash
# System-wide installation
curl -fsSL https://raw.githubusercontent.com/kyungw00k/tfpp/main/install.sh | sudo bash

# Verify installation
tfpp --help
```

### Custom Directory Installation

Install TFPP to a custom directory:

```bash
# Install to ~/bin
curl -fsSL https://raw.githubusercontent.com/kyungw00k/tfpp/main/install.sh | bash -s ~/bin

# Add to PATH if not already included
echo 'export PATH="$HOME/bin:$PATH"' >> ~/.bashrc
source ~/.bashrc
```

### Manual Installation

For more control over the installation:

1. **Download the JAR**:
   - Go to [releases page](https://github.com/kyungw00k/tfpp/releases)
   - Download the latest `tfpp.jar`

2. **Create wrapper script**:
   ```bash
   echo '#!/bin/bash
   java -jar /path/to/tfpp.jar "$@"' > /usr/local/bin/tfpp
   chmod +x /usr/local/bin/tfpp
   ```

3. **Test installation**:
   ```bash
   tfpp --version
   ```

## Your First Template

Let's create a simple example to verify everything works:

### Step 1: Create a Template

Create `welcome.html`:
```html
<!DOCTYPE html>
<html>
<head>
    <title>Welcome to [[${siteName}]]</title>
</head>
<body>
    <h1>Hello, [[${userName}]]!</h1>
    <p>Welcome to our [[${siteName}]] site.</p>
    <p>Today is [[${#dates.format(currentDate, 'MMMM dd, yyyy')}]]</p>
</body>
</html>
```

### Step 2: Create Data File

Create `data.json`:
```json
{
  "siteName": "TFPP Demo Site",
  "userName": "John Doe",
  "currentDate": "2024-01-15T10:30:00"
}
```

### Step 3: Process the Template

```bash
tfpp process welcome.html --data data.json --output result.html
```

### Step 4: View the Result

Open `result.html` in your browser to see the processed template with your data!

## What's Next?

- Learn about [Template Syntax](template-guide.md)
- Explore [Advanced Features](advanced-topics.md)
- Check out the [CLI Documentation](../app/src/main/kotlin/tfpp/cli/)