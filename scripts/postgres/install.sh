#!/bin/bash

image=postgres

docker pull ${image}

docker rm -f ${image}

docker run -d \
           --name ${image} \
           --restart unless-stopped \
           -e POSTGRES_USER=prod_database \
           -e POSTGRES_PASSWORD=QUFNRXNlY3JldDA0MDUyMDE5MzA4Cg== \
           -p 5432:5432 \
           ${image}