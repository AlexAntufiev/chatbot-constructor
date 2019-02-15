#!/usr/bin/env bash

mode=$1

if [ "$mode" != "test" ]; then
    echo "Mode must be set to 'test'"
    exit
fi

vpn_file=./vpn/vpn.env
docker_image=hwdsl2/ipsec-vpn-server
docker_container=vpn

docker pull ${docker_image}

docker rm -f ${docker_container}

chmod 700 ${vpn_file}

sudo modprobe af_key

docker run -d \
           --name ${docker_container} \
           --env-file ${vpn_file} \
           --restart=on-failure:5 \
           -p 500:500/udp \
           -p 4500:4500/udp \
           -v /lib/modules:/lib/modules:ro \
           --privileged \
           ${docker_image}