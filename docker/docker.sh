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

    send_message "${server_type} останавливается"
    docker stop ${application_name}
    send_message "${server_type} остановлен"

    echo "Set profile to ${server_type}"
    docker build --build-arg custom_profile=${server_type} \
                 -t ${application_name} \
                 .

    send_message "${server_type} запускается"
    docker run -d \
               --restart=on-failure:5 \
               -p 80:8090 \
               -v ~/${application_name}/logs:/logs \
               --name ${application_name} \
               --log-opt max-size=50m \
               --log-opt max-file=3 \
               ${application_name}


    sleep 25

    send_message "${server_type} запущен: http://${host}/index.html \n managing config: http://${host}:${config_service_port}/ui/dc1/services"

    docker image rm -f ${application_name}
    rm -f ${application_name}.jar Dockerfile run.sh
}

main