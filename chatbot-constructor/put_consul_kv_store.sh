#!/bin/bash

key=config/chatbot-constructor/data
value=consul/data/app_config.yml

docker exec -it consul sh -c "consul kv put ${key} @${value}"
