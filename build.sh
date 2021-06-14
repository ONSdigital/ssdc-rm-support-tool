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

mvn clean install