#!/bin/bash

path_to_config_file="./config.json"
server_name="TEST-1"
docker_image="wdijkerman/consul"
container_name="consul"

docker pull ${docker_image}

docker image rm -f ${container_name}

docker run  -h \
            -p 8400:8400 \
            -p 8500:8500 \
            -p 8600:53/udp \
            -h ${server_name} \
            -v ${path_to_config_file}:/consul/config/my_config.json:ro \
            --name ${container_name} \
            ${docker_image}
