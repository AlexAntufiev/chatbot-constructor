#!/bin/bash

app_name="chatbot-constructor"
ssh_path="/tmp/prod_ssh.pem"

npm i -g npm@6.7.0
./gradlew clean build
cp ./build/libs/chatbot-constructor-*.jar ./chatbot-constructor.jar

docker build -t $docker_user/chatbot-constructor .
echo $docker_password | docker login -u $docker_user --password-stdin
docker images
docker push $docker_user/chatbot-constructor

echo "${PROD_SSH}" | base64 --decode >${ssh_path}
chmod 600 ${ssh_path}

printf "POSTGRES_USER=${db_user}\nPOSTGRES_PASSWORD=${db_password}" > ./${app_name}/db.env
rsync -avz -e "ssh -i ${ssh_path}" ./${app_name}/ ${server_user}@${PROD_IP}:/home/${server_user}/${app_name}
time ssh -i ${ssh_path} ${server_user}@${PROD_IP} "cd ${app_name} && docker-compose up -d"

docker logout