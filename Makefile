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

cljfmt-check:
	clojure -M:dev -m cljfmt.main check

cljfmt-fix:
	clojure -M:dev -m cljfmt.main fix

# check clj library versions
ancient:
	@clojure -M:outdated --no-changes --skip=pom

# report on unused clj code
carve-report:
	clojure -M:carve --paths src dev test --report true --report-format :text

# remove unused clj code
carve:
	clojure -M:carve --paths src dev test

.PHONY: unit build nrepl clean
