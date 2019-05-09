#!/bin/bash

sudo mkdir -p /grafana_data
chown 472:472 /grafana_data
sudo mkdir -p /influxdb_data

docker-compose up -d