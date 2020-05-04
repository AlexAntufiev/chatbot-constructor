#!/bin/bash

app_name="chatbot-constructor"
user="ubuntu"

ssh_prod="chatbot.pem"
ip_prod="ec2-18-218-156-154.us-east-2.compute.amazonaws.com"

ssh_path=${ssh_prod}
server=${ip_prod}

rsync -avz -e "ssh -i ${ssh_path}" scripts/ ${user}@${server}:/home/${user}/${app_name}