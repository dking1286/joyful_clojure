#! /bin/bash
docker run \
  --name=local-postgres \
  --rm \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=mysecretpassword \
  -d \
  -p 8082:5432 \
  -v $HOME/docker/volumes/postgres:/var/lib/postgresql/data \
  postgres:alpine
