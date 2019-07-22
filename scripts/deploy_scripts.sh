#!/bin/bash

app_name="chatbot-constructor"
user="ubuntu"
server_type=$1

ssh_test="keys/TEST-1.pem"
ip_test="89.208.84.173"

ssh_prod="keys/cc-key.cer"
ip_prod="ec2-18-188-3-124.us-east-2.compute.amazonaws.com"

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