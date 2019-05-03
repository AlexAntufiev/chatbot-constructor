#!/usr/bin/env bash
docker_container_name=ipsec-vpn-server
docker pull hwdsl2/ipsec-vpn-server

sudo modprobe af_key

docker rm -f ${docker_container_name}

docker run \
    --name ${docker_container_name} \
    --env-file ./vpn.env \
    --restart=always \
    -p 500:500/udp \
    -p 4500:4500/udp \
    -v /lib/modules:/lib/modules:ro \
    -d --privileged \
    hwdsl2/ipsec-vpn-server