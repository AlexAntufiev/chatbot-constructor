FROM openjdk:11

ARG profile=development

COPY run.sh chatbot-constructor.jar /

EXPOSE 8090

VOLUME chatbot-constructor

ENTRYPOINT /run.sh -p ${profile} run