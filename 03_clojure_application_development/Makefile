server-dev:
	lein with-profile +dev ring server-headless

database-dev-init:
	docker run --name=local-postgres \
		-e POSTGRES_PASSWORD=mysecretpassword \
		-d \
		-p 8082:5432 \
		postgres:9.6-alpine

database-dev-start:
	docker start local-postgres

database-dev-connect:
	docker exec -it local-postgres psql -U postgres
