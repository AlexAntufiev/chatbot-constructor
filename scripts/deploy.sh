#!/bin/bash

app_name=chatbot-constructor
path_to_jar="../build/libs/"
jar_name="${app_name}.jar"
jar=${path_to_jar}
jar+=${jar_name}

pem_key_file="../keys/CentOS_Basic-1-1-10_10GB_1_id_rsa.pem"
user="centos"
server="89.208.84.173"

./build.sh

echo "Jar: "+${jar}
echo "Sending to: "+${server}
rsync -az --info=progress2 -e "ssh -i ${pem_key_file}" ${jar} ${user}@${server}:/home/centos/chatbot-constructor/ && echo "*** Complete ***"