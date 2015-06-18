#!/bin/bash
set -o errexit

main () {
    # 7000: intra-node communication
    # 7001: TLS intra-node communication
    # 7199: JMX
    # 9042: CQL
    # 9160: thrift service
    script_path=$(pwd)/script:/data/
    echo "Starting cassandra $script_path"
    docker run -d --name esports-cassandra  -v $script_path -p 44000:7000 -p 44001:7001 -p 44002:7199 -p 44003:9042 -p 44004:9160 cassandra:2.1

    echo "Running cassandra db script..."
    sleep 15
    docker exec esports-cassandra  bash -c 'cqlsh $CASSANDRA_PORT_9042_TCP_ADDR -f /data/init.cql'
}

main "$@"
