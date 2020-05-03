#!/bin/bash

app_name="chatbot-constructor"
ssh_deploy_file_test="/tmp/prod_ssh.pem"
ssh_deploy_file_prod="/tmp/prod_ssh.pem"
user="ubuntu"
server_type=$1
server=$2

if [ "$server_type" == "test" ]; then
    ssh_path=${ssh_deploy_file_test}
    db_password=MDYwNTIwMTlxd2VydHkK
elif [ "$server_type" == "prod" ]; then
    ssh_path=${ssh_deploy_file_prod}
    db_password=QUFNRXNlY3JldDA0MDUyMDE5MzA4Cg==
else
    echo "Incorrect server type"
    exit
fi

cp ./build/libs/chatbot-constructor* ./docker/
cd ./docker/
printf "profile=${server_type}" > app.env
printf "POSTGRES_USER=${server_type}\nPOSTGRES_PASSWORD=${db_password}" > db.env

time ssh -i ${ssh_path} ${user}@${server} "mkdir -p ${app_name} && cd ${app_name} && rm -f ${app_name}*"
rsync -avz -e "ssh -i ${ssh_path}" ./ ${user}@${server}:/home/${user}/${app_name}
#time ssh -i ${ssh_path} ${user}@${server} "cd ${app_name} && chmod +x docker.sh && \
#                                           nohup ./docker.sh ${server_type} ${server} > /dev/null 2>&1 &"