test:
	mvn clean verify jacoco:report

build:
	./build.sh

test-ui:
	cd ui && npm install && npm test -- --watchAll=false

run-dev-api: build
	docker run -e spring_profiles_active=docker --network=ssdcrmdockerdev_default --link ons-postgres:postgres -p 9999:9999 eu.gcr.io/ssdc-rm-ci/rm/ssdc-rm-support-tool:latest

run-dev-ui:
	cd ui && npm install && npm start