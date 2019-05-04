#!/bin/bash

node_address="192.168.9.152"
docker_image="consul"

docker pull ${docker_image}

docker rm -f ${docker_image}

docker run  -d \
            --net=host \
            --name ${docker_image} \
           --restart unless-stopped \
            -e CONSUL_BIND_INTERFACE=eth0 \
            ${docker_image} \
            agent \
            -server \
            -ui \
            -bootstrap-expect=1 \
            -client ${node_address}
