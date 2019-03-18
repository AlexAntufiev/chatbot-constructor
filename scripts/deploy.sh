#!/bin/bash

pem_key_file="../keys/CentOS_Basic-1-1-10_10GB_1_id_rsa.pem"
user="centos"
server="89.208.84.173"

rsync -az --info=progress2 $1 ${user}@${server}:/home/centos/chatbot-constructor/
#rsync -az --info=progress2 -e "ssh -i ${pem_key_file}" ${run_script} ${user}@${server}:/home/centos/chatbot-constructor/ && echo "*** Complete ***"
#ssh -i ${pem_key_file} ${user}@${server} "cd ./${app_name} && ./run.sh -p development start"