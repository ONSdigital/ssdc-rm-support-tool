#!/bin/sh
mkdir -p src/main/resources/static
rm -r src/main/resources/static/* || true
rm -r ui/build/* || true
cd ui || { echo "Unable to access ui directory"; exit 1; }
npm install

if ! npx npx eslint .; then
  echo "ESLint found issues"
  exit 1
fi

npm run build
cd ..
cp -r ui/build/* src/main/resources/static
rm -r ui/build/* || true

if [ "$SKIP_TESTS" = true ] ; then
  mvn clean install -Dmaven.test.skip=true -Dexec.skip=true -Djacoco.skip=true
else
  mvn clean install
fi
docker build . -t europe-west2-docker.pkg.dev/ssdc-rm-ci/docker/ssdc-rm-support-tool:latest
