#!/bin/sh
set -e

BASE_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
PROPS="$BASE_DIR/.mvn/wrapper/maven-wrapper.properties"

if [ ! -f "$PROPS" ]; then
  echo "Error: no se encontro $PROPS" >&2
  exit 1
fi

DIST_URL=$(sed -n 's/^distributionUrl=//p' "$PROPS" | tail -n 1)
if [ -z "$DIST_URL" ]; then
  echo "Error: distributionUrl no esta definido en maven-wrapper.properties" >&2
  exit 1
fi

DIST_FILE=${DIST_URL##*/}
DIST_NAME=${DIST_FILE%.zip}
DIST_NAME=${DIST_NAME%-bin}
MAVEN_USER_HOME=${MAVEN_USER_HOME:-"$HOME/.m2"}
DIST_PARENT="$MAVEN_USER_HOME/wrapper/dists"
MAVEN_HOME="$DIST_PARENT/$DIST_NAME"

if [ ! -x "$MAVEN_HOME/bin/mvn" ]; then
  mkdir -p "$DIST_PARENT"
  TMP_DIR=$(mktemp -d 2>/dev/null || mktemp -d -t mvnw)
  trap 'rm -rf "$TMP_DIR"' EXIT INT TERM
  ZIP_FILE="$TMP_DIR/$DIST_FILE"

  if command -v curl >/dev/null 2>&1; then
    curl -fsSL "$DIST_URL" -o "$ZIP_FILE"
  elif command -v wget >/dev/null 2>&1; then
    wget -q "$DIST_URL" -O "$ZIP_FILE"
  else
    echo "Error: necesitas curl o wget para descargar Maven." >&2
    exit 1
  fi

  if ! command -v unzip >/dev/null 2>&1; then
    echo "Error: necesitas unzip para instalar Maven." >&2
    exit 1
  fi

  unzip -q "$ZIP_FILE" -d "$DIST_PARENT"
  rm -rf "$TMP_DIR"
  trap - EXIT INT TERM
fi

exec "$MAVEN_HOME/bin/mvn" "$@"
