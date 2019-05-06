#!/bin/bash

mode=$1

if [ "$mode" != "test" ] && [ "$mode" != "prod" ]; then
    echo "Mode must be set and 'test' or 'prod'"
    exit
fi

application_name="chatbot-constructor"

mkdir -p ~/${application_name}

echo "Docker installing..."
./docker/install.sh
echo "Docker installed"

echo "Vpn installing..."
./vpn/install.sh ${mode}
echo "Vpn installed "

echo "Consul installing..."
./consul/install.sh ${mode}
echo "Consul installed"

echo "Postgres installing..."
./postgres/install.sh ${mode}
echo "Postgres installed"
