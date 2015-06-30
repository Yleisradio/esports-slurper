#!/bin/bash
set -o errexit

main () {

    docker run -d --name esports-mysql  -v ./:/data/  -e MYSQL_ROOT_PASSWORD=admin -e MYSQL_DATABASE=ebotv3 -e MYSQL_USER=ebotv3 -e MYSQL_PASSWORD=ebotv3 mysql/mysql-server:latest

    echo "Running mysql db script..."
    sleep 15
    docker exec esports-ebot  bash -c 'bash /data/post-install.sh'
}

main "$@"
