#!/bin/bash

app_name="chatbot-constructor"
user="centos"
server_type=$1

ssh_test="keys/TEST-1.pem"
ip_test="89.208.84.173"

ssh_prod="keys/PROD-1.pem"
ip_prod="89.208.84.33"


if [ "$server_type" == "test" ]; then
    ssh_path=${ssh_test}
    server=${ip_test}
elif [ "$server_type" == "prod" ]; then
    ssh_path=${ssh_prod}
    server=${ip_prod}
else
    echo "Incorrect server type"
    exit
fi

rsync -avz -e "ssh -i ${ssh_path}" scripts/ ${user}@${server}:/home/${user}/${app_name}