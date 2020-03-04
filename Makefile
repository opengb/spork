all: unit

unit: node_modules
	bin/kaocha

watch-unit: node_modules
	bin/kaocha --watch

build:
	clj --main cljs.main --compile-opts prd.cljs.edn --compile opengb.spork

repl:
	clj -A:dev:test:nrepl

lint:
	clj-kondo --lint dev src test

clean:
	rm -rf out target

node_modules: package.json
	npm install
	@touch node_modules

.PHONY: unit build nrepl clean
