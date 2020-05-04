#!/bin/bash

node_address="ec2-18-188-3-124.us-east-2.compute.amazonaw"

docker_image="consul"

docker pull ${docker_image}

docker rm -f ${docker_image}

docker run  -d \
            --net=host \
            --restart=on-failure:5 \
            -e CONSUL_BIND_INTERFACE=eth0 \
            ${docker_image} \
            agent \
            -server \
            -ui \
            -client \
            ${node_address}
