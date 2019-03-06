#!/bin/bash

app_name=chatbot-constructor
path_to_jar="../build/libs/"
jar_name="${app_name}.jar"
jar=${path_to_jar}
jar+=${jar_name}
run_script="../scripts/run.sh"

pem_key_file="../keys/CentOS_Basic-1-1-10_10GB_1_id_rsa.pem"
user="centos"
server="89.208.84.173"

./build.sh

echo "Jar: "+${jar}
echo "Sending to: "+${server}
rsync -az --info=progress2 -e "ssh -i ${pem_key_file}" ${jar} ${user}@${server}:/home/centos/chatbot-constructor/ && echo "*** Complete ***"
rsync -az --info=progress2 -e "ssh -i ${pem_key_file}" ${run_script} ${user}@${server}:/home/centos/chatbot-constructor/ && echo "*** Complete ***"
ssh -i ${pem_key_file} ${user}@${server} "cd ./${app_name} && ./run.sh -p development start"