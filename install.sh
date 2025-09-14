#!/bin/bash

# TFPP (Thymeleaf File Preprocessor) Smart Installation Script
# This script automatically detects local build or downloads from GitHub

set -e

# GitHub repository information
GITHUB_REPO="kyungw00k/tfpp"
GITHUB_RAW_URL="https://raw.githubusercontent.com/${GITHUB_REPO}/main"
GITHUB_RELEASES_URL="https://github.com/${GITHUB_REPO}/releases"

# Default installation directory
DEFAULT_INSTALL_DIR="/usr/local/bin"
INSTALL_DIR="${1:-$DEFAULT_INSTALL_DIR}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

# Check required tools for remote installation
check_remote_requirements() {
    if ! command -v curl >/dev/null 2>&1; then
        print_error "curl is required for remote installation but not found"
        echo "Please install curl or build TFPP locally"
        exit 1
    fi
}

# Check if Java is available
check_java() {
    if ! command -v java >/dev/null 2>&1; then
        print_error "Java is required but not found"
        echo "Please install Java (JDK 17 or later) and try again"
        exit 1
    fi
}

# Get latest release version from GitHub
get_latest_version() {
    print_step "Fetching latest version information..."
    
    local latest_version
    latest_version=$(curl -s "https://api.github.com/repos/${GITHUB_REPO}/releases/latest" | grep '"tag_name":' | sed -E 's/.*"tag_name": "([^"]+)".*/\1/' 2>/dev/null || echo "")
    
    if [ -z "$latest_version" ]; then
        print_warning "Could not fetch latest version from GitHub API"
        echo "main"
    else
        print_info "Latest version: $latest_version"
        echo "$latest_version"
    fi
}

# Download JAR from GitHub releases
download_jar_from_github() {
    local version="$1"
    
    if [ "$version" = "main" ]; then
        print_error "No releases available on GitHub"
        print_info "Please build TFPP locally:"
        echo "  git clone https://github.com/${GITHUB_REPO}.git"
        echo "  cd tfpp && ./gradlew jar && ./install.sh"
        exit 1
    fi
    
    local jar_url="${GITHUB_RELEASES_URL}/download/${version}/tfpp.jar"
    local temp_jar="/tmp/tfpp-${version}.jar"
    
    print_step "Downloading TFPP JAR from GitHub releases..."
    print_info "URL: $jar_url"
    
    if curl -fsSL "$jar_url" -o "$temp_jar"; then
        print_info "Downloaded JAR successfully"
        echo "$temp_jar"
    else
        print_error "Failed to download JAR from GitHub releases"
        print_info "Please check if the release exists or build from source"
        exit 1
    fi
}

# Main installation function
main() {
    print_info "🚀 TFPP (Thymeleaf File Preprocessor) Smart Installer"
    echo

    # Check if running as root for system installation
    if [ "$INSTALL_DIR" = "/usr/local/bin" ] && [ "$EUID" -ne 0 ]; then
        print_warning "Installing to system directory requires sudo privileges"
        print_info "System install: curl -fsSL ${GITHUB_RAW_URL}/install.sh | sudo bash"
        print_info "Custom install: curl -fsSL ${GITHUB_RAW_URL}/install.sh | bash -s ~/bin"
        print_info "Local install: sudo $0 (from repo directory)"
        exit 1
    fi

    # Check if Java is available
    check_java

    # Determine JAR source (local vs remote)
    local jar_file=""
    local tfpp_local_jar="app/build/libs/tfpp.jar"
    
    if [ -f "$tfpp_local_jar" ]; then
        # Local installation
        print_info "🏠 Found local JAR, installing from local build"
        jar_file="$tfpp_local_jar"
        print_info "Using: $jar_file"
    else
        # Remote installation
        print_info "🌐 Local JAR not found, downloading from GitHub"
        check_remote_requirements
        
        local version
        version=$(get_latest_version)
        jar_file=$(download_jar_from_github "$version")
    fi

    # Create installation directory if it doesn't exist
    if [ ! -d "$INSTALL_DIR" ]; then
        print_step "Creating installation directory: $INSTALL_DIR"
        mkdir -p "$INSTALL_DIR"
    fi

    # Install JAR
    local tfpp_install_jar="$INSTALL_DIR/tfpp.jar"
    print_step "Installing JAR to: $tfpp_install_jar"
    cp "$jar_file" "$tfpp_install_jar"
    
    # Clean up temp file if it was downloaded
    if [[ "$jar_file" == "/tmp/tfpp-"* ]]; then
        rm "$jar_file"
    fi

    # Create wrapper script
    local tfpp_script="$INSTALL_DIR/tfpp"
    print_step "Creating wrapper script: $tfpp_script"

    cat > "$tfpp_script" << 'EOF'
#!/bin/bash

# TFPP (Thymeleaf File Preprocessor) Wrapper Script
# Installed from https://github.com/kyungw00k/tfpp

# Determine the script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Set JAVA_HOME if not already set
export JAVA_HOME="${JAVA_HOME:-$(which java 2>/dev/null | xargs dirname 2>/dev/null | xargs dirname 2>/dev/null)}"

# Check if Java is available
if [ ! -x "${JAVA_HOME}/bin/java" ] && ! command -v java >/dev/null 2>&1; then
    echo "Error: Java not found. Please install Java or set JAVA_HOME." >&2
    exit 1
fi

# Determine Java executable
if [ -x "${JAVA_HOME}/bin/java" ]; then
    JAVA_CMD="${JAVA_HOME}/bin/java"
else
    JAVA_CMD="java"
fi

# TFPP JAR location (installed version)
TFPP_JAR="${SCRIPT_DIR}/tfpp.jar"

# Check if JAR exists
if [ ! -f "$TFPP_JAR" ]; then
    echo "Error: TFPP JAR not found at $TFPP_JAR" >&2
    echo "Please reinstall TFPP: curl -fsSL https://raw.githubusercontent.com/kyungw00k/tfpp/main/install.sh | bash" >&2
    exit 1
fi

# JVM options (can be overridden by environment variables)
JVM_OPTS="${TFPP_JVM_OPTS:--Xmx512m}"

# Execute TFPP
exec "$JAVA_CMD" $JVM_OPTS $TFPP_OPTS -jar "$TFPP_JAR" "$@"
EOF

    # Make script executable
    chmod +x "$tfpp_script"

    # Success message
    echo
    print_info "🎉 TFPP installed successfully!"
    print_info "Location: $tfpp_script"
    echo
    print_info "Usage examples:"
    echo "  tfpp process template.html --data data.json"
    echo "  tfpp process-dir templates/ output/ --data site.yaml"
    echo "  tfpp --help"
    echo

    # Check if installation directory is in PATH
    if ! echo "$PATH" | grep -q "$INSTALL_DIR"; then
        print_warning "Installation directory $INSTALL_DIR is not in your PATH"
        print_info "Add it to your PATH by running:"
        echo "  echo 'export PATH=\"$INSTALL_DIR:\$PATH\"' >> ~/.bashrc"
        echo "  source ~/.bashrc"
        echo
    fi

    print_info "For more information, visit: https://github.com/${GITHUB_REPO}"
}

# Run main function
main "$@"