FROM openjdk:11-jre-stretch

ENV app_name=chatbot-constructor*.jar

COPY $app_name /

EXPOSE 8090

ENTRYPOINT java -jar $app_name -Xms512m -Xmx512m --spring.profiles.active=prod
