#!/usr/bin/env bash
# Release AngularKt to Maven Central + the Gradle Plugin Portal.
set -o pipefail
cd "$(dirname "$0")"

if [ ! -f secrets.properties ]; then
  echo "secrets.properties not found — see PUBLISHING.md" >&2
  exit 1
fi

prop() { grep "^$1=" secrets.properties 2>/dev/null | head -1 | cut -d= -f2-; }

export ORG_GRADLE_PROJECT_mavenCentralUsername="$(prop mavenCentralUsername)"
export ORG_GRADLE_PROJECT_mavenCentralPassword="$(prop mavenCentralPassword)"
export GRADLE_PUBLISH_KEY="$(prop gradle.publish.key)"
export GRADLE_PUBLISH_SECRET="$(prop gradle.publish.secret)"

echo "==> Maven Central: angularkt + angularkt-processor (signed)"
./gradlew publishAndReleaseToMavenCentral

echo "==> Gradle Plugin Portal: io.github.vladkalyuzhny.angularkt"
./gradlew -p plugin publishPlugins

echo "Done. Tag it:  git tag v$(grep '^version=' gradle.properties | cut -d= -f2) && git push --tags"
