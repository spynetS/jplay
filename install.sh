#!/usr/bin/env sh

#!/bin/bash

set -e  # Exit on any error

APP_NAME="jplay"
VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
INSTALL_DIR="/usr/local/share/java/$APP_NAME"
BIN_PATH="/usr/local/bin/$APP_NAME"

echo "🔧 Building the Java project with Maven..."
mvn clean package

JAR_FILE="target/$APP_NAME-$VERSION.jar"

if [[ ! -f "$JAR_FILE" ]]; then
    echo "❌ Build failed or no JAR found."
    exit 1
fi

echo "📦 Installing JAR to $INSTALL_DIR"
sudo mkdir -p "$INSTALL_DIR"
sudo cp "$JAR_FILE" "$INSTALL_DIR/$APP_NAME-$VERSION.jar"

echo "🚀 Creating launcher at $BIN_PATH"
sudo tee "$BIN_PATH" > /dev/null <<EOF
#!/bin/bash
exec java -jar "$INSTALL_DIR/$APP_NAME-$VERSION.jar" "\$@"
EOF

sudo chmod +x "$BIN_PATH"

echo "✅ Installed! You can now run the app using: $APP_NAME"
