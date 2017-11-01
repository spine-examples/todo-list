#!/usr/bin/env bash

kafka/bin/zookeeper-server-start.sh config/zookeeper.properties &
kafka/bin/kafka-server-start.sh config/kafka-server.properties &
