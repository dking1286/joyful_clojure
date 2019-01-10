dev-server:
	make clean-outputs
	make mode=development foreign-libs
	make sass-build
	lein with-profile +fig,+dev figwheel

# Fill in production build

clean-outputs:
	rm -rf "resources/public/cljs-out"
	rm -rf "resources/public/sass-out"
	rm -rf "resources/public/webpack-out"
	rm -rf "target"

foreign-libs:
	npm install
	npx webpack --mode=$(mode)

sass-build:
	mkdir -p resources/public/sass-out
	sass resources/sass/style.scss resources/public/sass-out/style.css
