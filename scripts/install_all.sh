#!/bin/bash

echo "Docker installing... \n\n"
./docker/install.sh
echo "Docker installed \n\n"

echo "Consul installing... \n\n"
./consul/install.sh
echo "Consul installed \n\n"
