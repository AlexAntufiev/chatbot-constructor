#!/usr/bin/env bash

application_name="chatbot-constructor"

docker stop ${application_name}
docker rm -f ${application_name}
docker image rm -f ${application_name}

docker build -t ${application_name} .
docker run --entrypoint /run.sh run -p localhost:8090:8090 --name ${application_name} ${application_name}
