dev-server:
	lein with-profile +dev,+dev-local repl

dev-database:
	docker run \
	--name=joyful_clojure_05_postgres \
	--rm \
	-e POSTGRES_USER=postgres \
	-e POSTGRES_PASSWORD=mysecretpassword \
	-d \
	-p 8082:5432 \
	-v $$HOME/docker/volumes/postgres/joyful_clojure_05:/var/lib/postgresql/data \
	postgres:11.1-alpine

dev-database-connection:
	docker exec -it joyful_clojure_05_postgres psql -U postgres

dev-database-stop:
	docker stop joyful_clojure_05_postgres

prod-build:
	lein with-profile +prod uberjar

test:
	lein with-profile +test,+test-local test :once
