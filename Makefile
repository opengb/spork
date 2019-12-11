build:
	clj --main cljs.main --compile-opts prd.cljs.edn --compile opengb.spork

clean:
	rm -rf out

.PHONY: build clean
