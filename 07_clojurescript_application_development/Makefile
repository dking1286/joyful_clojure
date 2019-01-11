dev-server:
	lein clean
	make mode=development foreign-libs
	make sass-build
	lein with-profile +fig,+dev,+dev-local repl

# Fill in production build

foreign-libs:
	npm install
	npx webpack --mode=$(mode)

sass-build:
	mkdir -p target/public/sass-out
	sass resources/sass/style.scss target/public/sass-out/style.css
