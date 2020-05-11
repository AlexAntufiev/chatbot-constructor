FROM adoptopenjdk/openjdk11:jre-11.0.7_10-alpine

COPY chatbot-constructor*.jar /

EXPOSE 8090

ENTRYPOINT java -jar chatbot-constructor*.jar -Xms256m -Xmx256m
