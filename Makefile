all: unit

unit:
	bin/kaocha

build:
	clj --main cljs.main --compile-opts prd.cljs.edn --compile opengb.spork

clean:
	rm -rf out

.PHONY: unit build clean
