#!/bin/bash

app_name="chatbot-constructor"
ssh_test="keys/TEST-1.pem"
ssh_prod="keys/PROD-1.pem"
user="centos"
server_type=$1

if [ "$server_type" == "test" ]; then
    ssh_path=${ssh_test}
    server="89.208.84.173"
elif [ "$server_type" == "prod" ]; then
    ssh_path=${ssh_prod}
    server="89.208.84.33"
else
    echo "Incorrect server type"
    exit
fi

rsync -avz --progress -e "ssh -i ${ssh_path}" scripts ${user}@${server}:/home/${user}/