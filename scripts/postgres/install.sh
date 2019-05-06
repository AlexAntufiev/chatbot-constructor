#!/bin/bash

mode=$1

if [ "$mode" == "test" ]; then
    user=test_database
    password=MDYwNTIwMTlxd2VydHkK
elif [ "$mode" == "prod" ]; then
    user=prod_database
    password=QUFNRXNlY3JldDA0MDUyMDE5MzA4Cg==
else
    echo "Incorrect mode"
    exit
fi

image=postgres

docker pull ${image}

docker rm -f ${image}

docker run -d \
           --name ${image} \
           --restart=unless-stopped \
           -e POSTGRES_USER=${user} \
           -e POSTGRES_PASSWORD=${password} \
           -v /data:/var/lib/postgresql/data \
           -p 5432:5432 \
           ${image}