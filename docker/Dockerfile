FROM adoptopenjdk/openjdk11:jre-11.0.7_10-alpine

COPY chatbot-constructor.jar /app/
WORKDIR /app/
EXPOSE 8090

ENTRYPOINT java \
-XX:+UseG1GC \
-Xlog:gc*:file=gc.log \
-jar /app/chatbot-constructor*.jar \
-Xms256m -Xmx256m
