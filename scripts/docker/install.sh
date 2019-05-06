#!/bin/bash

user="centos"

sudo yum remove -y docker \
                   docker-ce \
                   docker-ce-cli \
                   docker-client \
                   docker-client-latest \
                   docker-common \
                   docker-latest \
                   docker-latest-logrotate \
                   docker-logrotate \
                   docker-engine

sudo yum install -y yum-utils \
                    device-mapper-persistent-data \
                    lvm2

sudo yum-config-manager \
     --add-repo \
     https://download.docker.com/linux/centos/docker-ce.repo

sudo yum install -y docker-ce \
                    docker-ce-cli \
                    containerd.io

sudo usermod -aG docker ${user}

sudo gpasswd -a ${user} docker

newgrp docker & echo

sudo systemctl daemon-reload

sudo systemctl start docker

sudo systemctl status docker