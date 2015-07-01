#!/bin/bash
set -o errexit

main () {
    echo "Starting esports-ebot $script_path"

    docker run -d --name esports-ebot --link esports-mysql:mysql -p 81:80 -p 12360:12360 esports-ebot

    sleep 15
    docker exec -it esports-ebot php /var/www/html/ebot-web/symfony cc
    docker exec -it esports-ebot php /var/www/html/ebot-web/symfony doctrine:build --all --no-confirmation
    docker exec -it esports-ebot php /var/www/html/ebot-web/symfony guard:create-user --is-super-admin admin@ebot admin admin
    docker exec -it esports-ebot screen -dmS ebotv3 php /home/ebot/ebot-csgo/bootstrap.php


}

main "$@"
