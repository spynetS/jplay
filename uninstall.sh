#!/usr/bin/env sh

APP_NAME="jplay"
VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
INSTALL_DIR="/usr/local/share/java/$APP_NAME"
BIN_PATH="/usr/local/bin/$APP_NAME"

echo "🗑️  Uninstalling $APP_NAME..."

if [ -f "$BIN_PATH" ]; then
  echo "❌ Removing launcher script at $BIN_PATH"
  sudo rm -f "$BIN_PATH"
else
  echo "⚠️  No launcher script found at $BIN_PATH"
fi

if [ -d "$INSTALL_DIR" ]; then
  echo "❌ Removing app files at $INSTALL_DIR"
  sudo rm -rf "$INSTALL_DIR"
else
  echo "⚠️  No install directory found at $INSTALL_DIR"
fi

echo "✅ Uninstallation complete."
