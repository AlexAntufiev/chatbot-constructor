FROM openjdk:11-jre-stretch

ENV profile=dev
ENV app_name=chatbot-constructor*.jar

COPY $app_name /

EXPOSE 8090

ENTRYPOINT java -jar $app_name -Xms10g -Xmx20g --spring.profiles.active=$profile
