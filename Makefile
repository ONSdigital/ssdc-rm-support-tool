build:
	./build.sh

build-no-test:
	SKIP_TESTS=true ./build.sh

test: test-mvn test-ui

test-mvn:
	mvn clean verify jacoco:report

test-ui:
	cd ui && npm install && npx eslint . && npm test -- --watchAll=false

run-dev-api: build
	docker run -e spring_profiles_active=docker --network=ssdcrmdockerdev_default --link ons-postgres:postgres -p 9999:9999 europe-west2-docker.pkg.dev/ssdc-rm-ci/docker/ssdc-rm-support-tool:latest

run-dev-ui:
	cd ui && npm install && npm start

format-check-mvn:
	mvn fmt:check

check-mvn:
	mvn fmt:check pmd:check

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

megalint:  ## Run the mega-linter.
	docker run --platform linux/amd64 --rm \
		-v /var/run/docker.sock:/var/run/docker.sock:rw \
		-v $(shell pwd):/tmp/lint:rw \
		oxsecurity/megalinter:v8

megalint-fix:  ## Run the mega-linter and attempt to auto fix any issues.
	docker run --platform linux/amd64 --rm \
		-v /var/run/docker.sock:/var/run/docker.sock:rw \
		-v $(shell pwd):/tmp/lint:rw \
		-e APPLY_FIXES=all \
		oxsecurity/megalinter:v8

clean_megalint: ## Clean the temporary files.
	rm -rf megalinter-reports

lint_check: clean_megalint megalint
