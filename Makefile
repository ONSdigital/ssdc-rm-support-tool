test:
	mvn clean verify jacoco:report

build:
	./build.sh

build-no-test:
	SKIP_TESTS=true ./build.sh

test-ui:
	cd ui && npm install && npx eslint . && npm test -- --watchAll=false

run-dev-api: build
	docker run -e spring_profiles_active=docker --network=ssdcrmdockerdev_default --link ons-postgres:postgres -p 9999:9999 eu.gcr.io/ssdc-rm-ci/rm/ssdc-rm-support-tool:latest

run-dev-ui:
	cd ui && npm install && npm start

format-check-mvn:
	mvn fmt:check

format-check-ui:
	$(MAKE) -C ui format-check

format-check: format-check-mvn format-check-ui

format-mvn:
	mvn fmt:format

format-ui:
	$(MAKE) -C ui format

format: format-mvn format-ui

package-audit-ui:
	$(MAKE) -C ui package-audit

docker-build:
    SKIP_TESTS=true ./build.sh