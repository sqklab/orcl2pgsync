#!/bin/sh

if [[ -z "$KAFKA_PORT" ]]; then
    export KAFKA_PORT=9092
fi

if [[ -z "$KAFKA_ADVERTISED_PORT" && \
  -z "$KAFKA_LISTENERS" && \
  -z "$KAFKA_ADVERTISED_LISTENERS" && \
  -S /var/run/docker.sock ]]; then
    KAFKA_ADVERTISED_PORT=$(docker port "$(hostname)" $KAFKA_PORT | sed -r 's/.*:(.*)/\1/g' | head -n1)
    export KAFKA_ADVERTISED_PORT
fi

if [[ -z "$KAFKA_BROKER_ID" ]]; then
    if [[ -n "$BROKER_ID_COMMAND" ]]; then
        KAFKA_BROKER_ID=$(eval "$BROKER_ID_COMMAND")
        export KAFKA_BROKER_ID
    else
        # By default auto allocate broker ID
        export KAFKA_BROKER_ID=-1
    fi
fi

if [[ -z "$KAFKA_LOG_DIRS" ]]; then
    export KAFKA_LOG_DIRS="/kafka/kafka-logs-$HOSTNAME"
fi

# Try and configure minimal settings or exit with error if there isn't enough information
if [[ -z "$KAFKA_ADVERTISED_HOST_NAME$KAFKA_LISTENERS" ]]; then
    if [[ -n "$KAFKA_ADVERTISED_LISTENERS" ]]; then
        echo "ERROR: Missing environment variable KAFKA_LISTENERS. Must be specified when using KAFKA_ADVERTISED_LISTENERS"
        exit 1
    elif [[ -z "$HOSTNAME_VALUE" ]]; then
        echo "ERROR: No listener or advertised hostname configuration provided in environment."
        echo "       Please define KAFKA_LISTENERS / (deprecated) KAFKA_ADVERTISED_HOST_NAME"
        exit 1
    fi

    # Maintain existing behaviour
    # If HOSTNAME_COMMAND is provided, set that to the advertised.host.name value if listeners are not defined.
    export KAFKA_ADVERTISED_HOST_NAME="$HOSTNAME_VALUE"
fi

(
    function updateConfig() {
        key=$1
        value=$2
        file=$3

        # Omit $value here, in case there is sensitive information
        echo "[Configuring] '$key' in '$file'"

        # If config exists in file, replace it. Otherwise, append to file.
        if grep -E -q "^#?$key=" "$file"; then
            sed -r -i "s@^#?$key=.*@$key=$value@g" "$file" #note that no config values may contain an '@' char
        else
            echo "$key=$value" >> "$file"
        fi
    }

    # Fixes #312
    # KAFKA_VERSION + KAFKA_HOME + grep -rohe KAFKA[A-Z0-0_]* /opt/kafka/bin | sort | uniq | tr '\n' '|'
    EXCLUSIONS="|KAFKA_VERSION|KAFKA_HOME|KAFKA_DEBUG|KAFKA_GC_LOG_OPTS|KAFKA_HEAP_OPTS|KAFKA_JMX_OPTS|KAFKA_JVM_PERFORMANCE_OPTS|KAFKA_LOG|KAFKA_OPTS|"

    # Read in env as a new-line separated array. This handles the case of env variables have spaces and/or carriage returns. See #313
    IFS=$'\n'
    updateConfig "advertised.listeners" "PLAINTEXT://${KAFKA_ADVERTISED_HOST_NAME}:${KAFKA_PORT}" "./config/server.properties"

    for VAR in $(env)
    do
        env_var=$(echo "$VAR" | cut -d= -f1)
        if [[ "$EXCLUSIONS" = *"|$env_var|"* ]]; then
            echo "Excluding $env_var from broker config"
            continue
        fi

        if [[ $env_var =~ ^KAFKA_ ]]; then
            kafka_name=$(echo "$env_var" | cut -d_ -f2- | tr '[:upper:]' '[:lower:]' | tr _ .)
            updateConfig "$kafka_name" "${!env_var}" "./config/server.properties"
        fi
    done
)

echo "------------ clone new connector -------------"
cp ./config/connect-distributed.properties ./config/connect-distributed2.properties
echo 'listeners=HTTP://:8084' &>> config/connect-distributed2.properties

echo "------------ START ZOOKEEPER -------------"
nohup ./bin/zookeeper-server-start.sh  ./config/zookeeper.properties > zookeeper.log &
sleep 10

echo "------------ START Kafka SERVER -------------"
nohup ./bin/kafka-server-start.sh      ./config/server.properties > server-kafka.log &
sleep 10

nohup ./create-connector.sh > create-connector.log &
nohup ./create-secret.sh > create-secret.log &

echo "------------ START CONNECTOR 2 -------------"
nohup ./bin/connect-distributed.sh   ./config/connect-distributed2.properties > connector2.log &

echo "------------ START CONNECTOR -------------"
 ./bin/connect-distributed.sh     ./config/connect-distributed.properties


