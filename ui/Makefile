format:
	npx prettier --write .

format-check:
	npx prettier --check .

package-audit:
	npx audit-ci --config ./audit-ci.json --moderate --skip-dev

package-audit-dev:
	npx audit-ci --config ./audit-ci.json --moderate

install:
	npm install