#!/bin/bash
set -o errexit

main () {
    echo "Starting esports-ebot $script_path"

    docker run -d --name esports-ebot --link esports-mysql:mysql -p 81:80 esports-ebot

    sleep 15
    docker exec -it esports-ebot bash php /home/ebot/ebot-web/symfony cc
    docker exec -it esports-ebot bash php /home/ebot/ebot-web/symfony doctrine:build --all --no-confirmation
    docker exec -it esports-ebot bash php /home/ebot/ebot-web/symfony guard:create-user --is-super-admin admin@ebot admin admin


}

main "$@"
