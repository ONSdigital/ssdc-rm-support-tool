#!/bin/sh
mkdir -p src/main/resources/static
rm -r src/main/resources/static/* || true
rm -r ui/build/* || true
cd ui
npm install
npm run build
cd ..
cp -r ui/build/* src/main/resources/static
rm -r ui/build/* || true

if [ "$SKIP_TESTS" = true ] ; then
  mvn clean install -Dmaven.test.skip=true -DdockerCompose.skip=true
else
  mvn clean install
fi
