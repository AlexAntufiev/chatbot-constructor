#!/bin/bash

app_name="chatbot-constructor"
ssh_path="/tmp/prod_ssh.pem"

echo "${PROD_SSH}" | base64 --decode >${ssh_path}
chmod 600 ${ssh_path}

cp ./build/libs/chatbot-constructor* ./docker/
cd ./docker/
printf "POSTGRES_USER=${db_user}\nPOSTGRES_PASSWORD=${db_password}" > db.env

time ssh -i ${ssh_path} ${server_user}@${PROD_IP} "mkdir -p ${app_name} && cd ${app_name} && rm -f ${app_name}*"
rsync -avz -e "ssh -i ${ssh_path}" ./ ${server_user}@${PROD_IP}:/home/${server_user}/${app_name}
time ssh -i ${ssh_path} ${server_user}@${PROD_IP} "cd ${app_name} && docker-compose build --force-rm app && docker-compose up -d"