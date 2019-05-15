#!/bin/bash

mode=$1

if [ "$mode" == "test" ]; then
    node_address="192.168.9.152"
elif [ "$mode" == "prod" ]; then
    node_address="192.168.23.55"
else
    echo "Incorrect mode"
    exit
fi

docker_image="consul"

docker pull ${docker_image}

docker rm -f ${docker_image}

docker run  -d \
            --net=host \
            --name ${docker_image} \
            --restart=on-failure:5 \
            -e CONSUL_BIND_INTERFACE=eth0 \
            ${docker_image} \
            agent \
            -server \
            -ui \
            -bootstrap-expect=1 \
            -client \
            ${node_address}
