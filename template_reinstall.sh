#!/bin/bash

export DB_ADMIN_PASSWORD=
export DB_SERVICE_USER=
export DB_SERVICE_PASSWORD=
export DB_LIQUIBASE_USER=
export DB_LIQUIBASE_PASSWORD=
export AWS_S3_BUCKET=
export SPRING_ADMIN_PASSWORD=
export AWS_ACCESS_KEY_ID=
export AWS_SECRET_ACCESS_KEY=

docker rm credible-badger-server -f
docker image rm credible-badger-server
docker load -i export.tar
