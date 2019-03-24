#!/usr/bin/env bash

application_name="chatbot-constructor"
token="QeFH2Jalc16THlx9YcNBDhtdx_u3uHOeDp8y8P20pT4"
chat_id="-70460825709429"
url="https://botapi.tamtam.chat/messages?access_token=${token}&chat_id=${chat_id}"
server_name="TEST-1"
host="89.208.84.173"
port="8090"
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
    docker run -d -p 8090:8090 --name ${application_name} ${application_name}
    send_message "${server_name} запущен: http://${host}:${port}/index.html"
}

main