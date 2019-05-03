#!/bin/bash

app_name="chatbot-constructor"
pem_key_file="keys/TEST-1.pem"
ssh_deploy_file_test="/tmp/deploy_rsa"
ssh_deploy_file_prod="/tmp/prod_ssh"
user="centos"
server_type=$1
server=$2

if [ "$server_type" == "test" ]; then
    ssh_path=${ssh_deploy_file_test}
elif [ "$server_type" == "prod" ]; then
    ssh_path=${ssh_deploy_file_prod}
else
    echo "Incorrect server type"
fi

cp ./build/libs/chatbot-constructor.jar ./docker/chatbot-constructor.jar
rsync -avz --progress -e "ssh -i ${ssh_path}" ./docker/ ${user}@${server}:/home/${user}/${app_name}
time ssh -i ${ssh_path} ${user}@${server} "cd ${app_name} && \
                                           chmod +x docker.sh run.sh && \
                                           nohup ./docker.sh ${server_type} ${server} > /dev/null 2>&1 &"