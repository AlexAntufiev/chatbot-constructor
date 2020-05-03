#!/usr/bin/env bash

application_name="chatbot-constructor"
token="QeFH2Jalc16THlx9YcNBDhtdx_u3uHOeDp8y8P20pT4"
chat_id="-70460825709429"
url="https://botapi.tamtam.chat/messages?access_token=${token}&chat_id=${chat_id}"
server_type=$1
host=$2
config_service_port="8500"
body=""

function set_message(){
    local message=$*
    if [[ ${message} != "" ]];
    then
        body="{\"text\":\"${message}\"}"
    else
        echo "No message"
    fi
}

function send_message() {
    set_message $*
    echo ${body} | curl -d @- -H "Content-Type: application/json" -X POST ${url}
}

function main() {
    if [ "$server_type" != "test" ] && [ "$server_type" != "prod" ]; then
        echo "Mode must be set and 'test' or 'prod'"
        exit
    fi

    if [ -z "$host" ] && [ "$server_type" != "local" ]; then
        echo "host must be set or be 'local'"
        exit
    fi

    if [[ $(docker-compose ps) != *"db"* ]]; then
#        send_message "${server_type} будет создан"
        docker-compose build app
        docker-compose up -d
#        send_message "${server_type} создался: http://${host}/index.html \n managing config: http://${host}:${config_service_port}/ui/dc1/services"
    else
#        send_message "${server_type} будет остановлен"
        docker-compose stop app
        docker-compose rm -f app
        docker image rm -f chatbot-constructor_app
#        send_message "${server_type} остановлен"

        docker-compose build app
        docker-compose up -d app
#        send_message "${server_type} перезапустился: http://${host}/index.html \n managing config: http://${host}:${config_service_port}/ui/dc1/services"
    fi
}

main