#!/bin/bash

app_name="chatbot-constructor"
ssh_deploy_file_test="/tmp/test_ssh"
ssh_deploy_file_prod="/tmp/prod_ssh"
user="centos"
server_type=$1
server=$2
db_password=MDYwNTIwMTlxd2VydHkK

if [ "$server_type" == "test" ]; then
    ssh_path=${ssh_deploy_file_test}
elif [ "$server_type" == "prod" ]; then
    ssh_path=${ssh_deploy_file_prod}
else
    echo "Incorrect server type"
    exit
fi

cp ./build/libs/chatbot-constructor.jar ./docker/chatbot-constructor.jar
cd ./docker/
printf "profile=${server_type}" > app.env
printf "POSTGRES_USER=${server_type}\nPOSTGRES_PASSWORD=${db_password}" > db.env

rsync -avz --progress -e "ssh -i ${ssh_path}" ./ ${user}@${server}:/home/${user}/${app_name}
time ssh -i ${ssh_path} ${user}@${server} "cd ${app_name} && \
                                           chmod +x docker.sh && \
                                           nohup ./docker.sh ${server_type} ${server} > /dev/null 2>&1 &"