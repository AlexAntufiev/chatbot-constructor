#!/bin/bash

app_name="chatbot-constructor"
pem_key_file="keys/CentOS_Basic-1-1-10_10GB_1_id_rsa.pem"
ssh_deploy_file="/tmp/deploy_rsa"
ssh_path=${ssh_deploy_file}
user="centos"
server="89.208.84.33"
#server="89.208.84.173"

cp ./build/libs/chatbot-constructor.jar ./docker/chatbot-constructor.jar
chmod +x scripts/deploy.sh
rsync -avz --progress -e "ssh -i ${ssh_path}" ./docker/ ${user}@${server}:/home/${user}/${app_name}
time ssh -i ${ssh_path} ${user}@${server} "cd ${app_name} && ls -l && chmod +x docker.sh run.sh && nohup ./docker.sh > /dev/null 2>&1 &"
