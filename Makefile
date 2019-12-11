all: unit

unit: node_modules
	bin/kaocha

build:
	clj --main cljs.main --compile-opts prd.cljs.edn --compile opengb.spork

clean:
	rm -rf out

node_modules: package.json
	npm install
	@touch node_modules

.PHONY: unit build clean
