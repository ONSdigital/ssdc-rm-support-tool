#!/bin/sh

# Set the container runtime based on architecture, default to docker for amd64 and podman for arm64
DOCKER=${DOCKER:-$(if [ "$(uname -m)" = "arm64" ]; then echo podman; else echo docker; fi)}

mkdir -p src/main/resources/static
rm -r src/main/resources/static/* || true
rm -r ui/build/* || true
cd ui || { echo "Unable to access ui directory"; exit 1; }
npm install

if ! npx eslint .; then
  echo "ESLint found issues"
  exit 1
fi

npm run build
cd ..
cp -r ui/build/* src/main/resources/static
rm -r ui/build/* || true

if [ "$SKIP_TESTS" = true ] ; then
  CONTAINER_CLI=$DOCKER mvn clean install -Dmaven.test.skip=true -Dexec.skip=true -Djacoco.skip=true
else
  CONTAINER_CLI=$DOCKER mvn clean install
fi

$DOCKER build . --platform linux/amd64 -t europe-west2-docker.pkg.dev/ssdc-rm-ci/docker/ssdc-rm-support-tool:latest

