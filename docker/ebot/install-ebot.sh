#!/bin/bash

mkdir /home/install
# cd /home/install
# wget http://be2.php.net/get/php-5.5.15.tar.bz2/from/this/mirror -O php-5.5.15.tar.bz2
# tar -xjvf php-5.5.15.tar.bz2
# cd php-5.5.15
# ./configure --prefix /usr/local --with-mysql --enable-maintainer-zts --enable-sockets
# make
# make install

# Installed via libpthread-stubs0-dev
# cd /home/install
# wget http://pecl.php.net/get/pthreads-2.0.7.tgz
# tar -xvzf pthreads-2.0.7.tgz
# cd pthreads-2.0.7
# /usr/local/bin/phpize
# ./configure
# make
# make install
# echo 'date.timezone = Europe/Helsinki' >> /usr/local/lib/php.ini
# echo 'extension=pthreads.so' >> /etc/php.ini

mkdir /home/ebot

cd /home/ebot
wget https://github.com/deStrO/eBot-CSGO/archive/threads.zip
unzip threads.zip
mv eBot-CSGO-threads ebot-csgo

cd /home/ebot/ebot-csgo

curl -sS https://getcomposer.org/installer | php
mv composer.phar /usr/local/bin/composer
composer install
npm install socket.io@0.9.12 archiver formidable


# edit config config/config.ini with IP/PORT and MySQL access
cd /home/ebot
wget https://github.com/deStrO/eBot-CSGO-Web/archive/master.zip
unzip master.zip
mv eBot-CSGO-Web-master ebot-web
cd ebot-web
mkdir /home/ebot/ebot-web/cache

cp config/app_user.yml.default config/app_user.yml

# edit config config/app_user.yml with ebot_ip and ebot_port
# edit database config/database.yml
# php5 symfony cc
# php5 symfony doctrine:build --all --no-confirmation
# php5 symfony guard:create-user --is-super-admin admin@ebot admin admin

# To start ebot daemon
# php /home/ebot/ebot-csgo/bootstrap.php
