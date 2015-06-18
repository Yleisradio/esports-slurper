#!/bin/bash
set -o errexit

main () {
    echo "Starting cassandra $script_path"
    docker run -d --name esports-slurper --link esports-cassandra:cassandra -p 3000:3000 -p 8081:8081 esports-slurper

}

main "$@"
