#!/bin/bash

path_to_gradlew="./../"

cd ${path_to_gradlew}

echo "*** Build project ***"
./gradlew clean test bootJar && echo "*** Build complete ***"