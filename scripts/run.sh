#!/usr/bin/env bash

application_name="chatbot-constructor"
application_start_type="background"
application_command=""
application_arguments=""
application_port="8090"

profile_requested=false
profile_name="development"

debug_requested=false
debug_port="5005"

help_requested=false
help_arguments=""

JAVA_MIN_MEM="128M"
JAVA_MAX_MEM="512M"

trap "error 'Interrupted!'; stopApplication" SIGINT
trap "error 'Critical error!'" ERR

jvm_arguments="-Xms${JAVA_MIN_MEM} -Xmx${JAVA_MAX_MEM}"

function error {
    echo "ERROR: ${1}" >&2
    exit ${2:-255}
}

function checkPidFileExists {
    if ! [[ -f "${application_name}.pid" ]]; then
        echo "PID file does not exists."
        return 1
    fi

    process_id=$(<${application_name}.pid)

    return 0
}

function checkApplicationStarted {
    if ! [[ -e "/proc/${process_id}" ]]; then
        echo "WARNING: PID file exists, but process does not. Consider removing PID file manually" >&2
        return 1
    fi

    return 0
}

function start {

    if checkPidFileExists > /dev/null && checkApplicationStarted; then
        echo "Application already started [PID: ${process_id}]."
        return
    fi

    if ! [[ -f "${application_name}.jar" ]]; then
        error "Application executable [${application_name}.jar] not accessible or does not exists"
    fi

    application_arguments="--server.port=${application_port} ${application_arguments}"

    startup_command="java ${jvm_arguments} -jar ${application_name}.jar ${application_arguments}"

    if [[ "${application_start_type}" == "background" ]]; then
        startup_command="nohup ${startup_command} > /dev/null 2>&1 &"
        echo "Starting an application in background: ${startup_command}"
    else
        echo "Starting an application: ${startup_command}"
    fi

    eval "${startup_command}"
}

function stop {
    if ! checkPidFileExists || ! checkApplicationStarted; then
        echo "Application already stopped"
        return
    fi

    stopApplication
}

function stopApplication {
    if [[ -f "${application_name}.pid" ]]; then
        process_id=$(<${application_name}.pid)
        echo "Stopping an application [PID: ${process_id}]..."
        kill ${process_id}

        echo "Waiting for application to stop..."
        while [[ -f "${application_name}.pid" ]]; do
            sleep 0.1
        done

        echo "Application stopped"
    else
        echo "PID file not found. Assuming application has stopped"
    fi
}

function printStatus {
    if ! checkPidFileExists || ! checkApplicationStarted; then
        echo "Application status: stopped."
        return
    else
        echo "Application status: running [${process_id}]."
    fi

     # If 'cURL' installed - try request for additional status information
    if [ -x "$(command -v curl)" ]; then
        status_url="http://localhost:${application_port}/actuator/health"
        status_information="$(curl -s -H Accept=application/json ${status_url} | python -m json.tool)"

        if [[ ${?} == 0 ]]; then
            echo -e "\nAdditional application information [JSON]:"
            echo "${status_information}"
        fi
    fi
}

function printHelp {
    echo "Help:"

    case "${help_arguments}" in
        "usage")
            printUsageHelp;;
        "options")
            printOptionsHelp;;
        "commands")
            printCommandsHelp;;
        "start")
            printStartHelp;;
        "")
            printUsageHelp
            printOptionsHelp
            printCommandsHelp;;
        *)
            error "Unknown help chapter. See: ${0} -h";;
    esac
}

function printUsageHelp {
    echo "Usage: ${0} [options] <command> [arguments]"
}

function printOptionsHelp {
    echo "Available options:"
    echo "  -d, --debug [port]      Enables debug mode for an application. Optionally, debugging port can be supplied"
    echo "  -p, --profile [profile] Choose profile for an application."
    echo "  --foreground            Starts an application in current session/terminal. Equivalent to 'run' command"
    echo "  -h, --help [chapter]    Prints help information"
    echo "                          Available help chapters: usage, options, commands, start, java"
}

function printCommandsHelp {
    echo "Available commands:"
    echo "  run      Starts an application in current session/terminal"
    echo "  start    Starts an application"
    echo "  stop     Stops an application"
    echo "  restart  Restarts an application"
    echo "  status   Returns status of application"
}

function printStartHelp {
    echo "An application could be started in a few different ways."
    echo "First and simplest way is just run shell script with 'start' command ('${0} start'). This will run an application on default port in background."
    echo ""
    echo "Second way - run an application in current shell. It can be done using 2 similar methods:"
    echo "  Using 'run' command - '${0} run'"
    echo "  Using '--foreground' option - '${0} --foreground start'"
    echo ""
    echo "If an application already running it can be restated by command 'restart': '${0} restart'"
    echo ""
    echo "Additional arguments for an application can be supplied though adding arguments to this shell script after setting command."
    echo "If an application arguments contains dashes '-' or any other special symbols, this arguments should be written after special double dash '--' argument."
    echo "For example, start an application with different logs folder: '${0} start -- --logs.dir=/var/log/name'"
    echo ""
    echo "An application can be started in debug mode, with possibility to attach debugger to JVM running an application. Use '-d' or '--debug' option to run an application in debug mode. Optionally, JVM debugging port can be set by argument of debug option. By default this port number is 5005."
    echo ""
    echo "By the way, options can be combined with other options and with commands."
    echo "For example, restart an application in foreground, which currently running in background, on different port can be done in one command: '${0} --foreground -p 1234 restart'"
}

function main {
    if [[ -z "${1}" ]]; then
        error "At least 1 argument must be supplied. See: ${0} -h"
    fi

    short_options="h:d:p:"
    long_options="help,debug:,profile:,foreground"
    args=$(getopt -o "${short_options}" -l "${long_options}" -- "$@");

    if [ $? -ne 0 ]; then
        error "Usage: ${0} [options] command [arguments]"
    fi

    eval set -- "${args}";

    while true; do
        case "${1}" in
            -d|--debug)
                shift;
                debug_requested=true
                if [ -n "$1" ]; then
                    if [[ "${1}" =~ ^[0-9]+$ ]]; then
                        if [[ ${1} -lt 1 || ${1} -gt 65535 ]]; then
                            error "Given debug port should be in range of 1 to 65535"
                        fi
                        debug_port="${1}"
                    fi
                    shift;
                fi

                echo "Application will be started with enabled debugging"
                echo "Debug port set to ${debug_port}"
                ;;
            -p|--profile)
                shift;
                profile_requested=true
                if [ -n "$1" ]; then
                    if [[ "${1}" -ne "development" || "${1}" -ne "production" ]]; then
                        error "Given profile should be 'development' or 'production'"
                    fi
                    profile_name="${1}"
                    shift;
                fi

                echo "Application will be started with profiling"
                echo "Profile set to ${profile_name}"
                ;;
            --foreground)
                shift;
                application_start_type="foreground"
                echo "Application will be started in current shell"
                ;;
            -h|--help)
                shift;
                help_requested=true
                help_arguments="${1}"
                printHelp
                exit;;
            --)
                shift;
                break;
                ;;
            *)
                application_command=${1}
                application_arguments=${@:3}
                if [[ ! -z "${application_arguments}" ]]; then
                    echo "Application args: ${application_arguments}"
                fi
                break;;
        esac
    done

    if ${help_requested}; then
        application_command="help"
    fi

    if ${debug_requested}; then
        jvm_arguments="${jvm_arguments} -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=${debug_port}"
    fi

    if ${profile_requested}; then
        application_arguments="${application_arguments} --spring.profiles.active=${profile_name}"
    fi

    case ${1} in
        "run")
            application_start_type="foreground"
            start;;
        "start")
            start;;
        "stop")
            stop;;
        "restart")
            stop && start;;
        "status")
            printStatus;;
        *)
            error "Unknown command '${1}'. See help for available commands: ${0} -h commands";;
    esac
}

main ${@}