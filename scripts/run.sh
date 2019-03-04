#!/usr/bin/env bash

application_name="chatbot-constructor"
startup="java -Xms128M -Xmx512M -jar ${application_name}.jar"

startup_background="nohup ${startup} > /dev/null 2>&1 &"

eval "${startup_background}" && echo "Application started"