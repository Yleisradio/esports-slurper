#!/bin/bash
set -o errexit

main () {

    script_path=$(pwd):/data/
    docker run -d --name esports-mysql  -v $script_path  -e MYSQL_ROOT_PASSWORD=admin -e MYSQL_DATABASE=ebotv3 -e MYSQL_USER=ebotv3 -e MYSQL_PASSWORD=ebotv3 mysql/mysql-server:latest

}

main "$@"
