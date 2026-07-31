#!/bin/sh
# Gradle Wrapper script - uses locally installed Gradle
APP_BASE_NAME=`basename "$0"`

# Try to find a usable Gradle installation
if [ -n "$GRADLE_HOME" ] && [ -x "$GRADLE_HOME/bin/gradle" ]; then
    GRADLE_CMD="$GRADLE_HOME/bin/gradle"
elif [ -x "/root/.local/share/mise/installs/gradle/8.14.4/bin/gradle" ]; then
    GRADLE_CMD="/root/.local/share/mise/installs/gradle/8.14.4/bin/gradle"
elif command -v gradle >/dev/null 2>&1; then
    GRADLE_CMD="gradle"
else
    echo "Error: Could not find a usable Gradle installation." >&2
    exit 1
fi

exec "$GRADLE_CMD" "$@"
