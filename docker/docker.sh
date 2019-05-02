#!/usr/bin/env bash

application_name="chatbot-constructor"
token="QeFH2Jalc16THlx9YcNBDhtdx_u3uHOeDp8y8P20pT4"
chat_id="-70460825709429"
url="https://botapi.tamtam.chat/messages?access_token=${token}&chat_id=${chat_id}"
server_name="TEST-1"
host="89.208.84.173"
port="80"
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
    send_message "${server_name} останавливается"
    docker stop ${application_name}
    docker rm -f ${application_name}
    docker image rm -f ${application_name}
    send_message "${server_name} остановлен"

    docker build -t ${application_name} .

    send_message "${server_name} запускается"
    docker run -d \
               -p 80:8090 \
               -v ~/${application_name}/logs:/logs \
               --name ${application_name} \
               --log-opt max-size=50m \
               --log-opt max-file=3 \
               ${application_name}

    sleep 40

    send_message "${server_name} запущен: http://${host}:${port}/index.html \n managing config: http://${host}:${config_service_port}/ui/dc1/services"
}

main