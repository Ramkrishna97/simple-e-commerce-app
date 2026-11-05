#!/bin/sh
APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
JAVA_EXE="${JAVA_HOME:+$JAVA_HOME/bin/}java"
exec "$JAVA_EXE" -classpath "$APP_HOME/gradle/wrapper/gradle-wrapper-main.jar:$APP_HOME/gradle/wrapper/gradle-wrapper-shared.jar" org.gradle.wrapper.GradleWrapperMain "$@"
