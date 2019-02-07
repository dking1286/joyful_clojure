dev-server:
	lein with-profile +dev,+dev-local ring server-headless

dev-database:
	docker run \
		--name=local-postgres \
		--rm \
		-e POSTGRES_USER=postgres \
		-e POSTGRES_PASSWORD=mysecretpassword \
		-d \
		-p 8082:5432 \
		-v $$HOME/docker/volumes/postgres:/var/lib/postgresql/data \
		postgres:alpine

dev-database-connection:
	docker exec -it local-postgres psql -U postgres

prod-build:
	lein with-profile +prod uberjar

test:
	lein with-profile +test,+test-local test :once
