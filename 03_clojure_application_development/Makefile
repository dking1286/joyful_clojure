dev-server:
	lein with-profile +dev ring server-headless

dev-database:
	docker run \
		--name=local-postgres \
		--rm \
		-e POSTGRES_USER=postgres \
		-e POSTGRES_PASSWORD=mysecretpassword \
		-d \
		-p 8082:5432 \
		-v $$HOME/docker/volumes/postgres:/var/lib/postgresql/data \
		postgres:9.6-alpine

dev-database-connection:
	docker exec -it local-postgres psql -U postgres

prod-build:
	lein with-profile +prod uberjar
