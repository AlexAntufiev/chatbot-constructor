FROM openjdk:11-jre-stretch

COPY chatbot-constructor*.jar /

EXPOSE 8090

ENTRYPOINT java -jar chatbot-constructor*.jar -Xms256m -Xmx256m
