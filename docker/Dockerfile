FROM openjdk:11

ARG custom_profile=development

ENV profile=$custom_profile

COPY run.sh chatbot-constructor.jar /

EXPOSE 8090

VOLUME chatbot-constructor

ENTRYPOINT /run.sh -p ${profile} run