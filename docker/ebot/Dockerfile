FROM ubuntu:14.04
MAINTAINER Jussi Pöri / Yleisradio

RUN apt-get update && apt-get -qq upgrade -y
RUN apt-get install -y wget unzip nodejs npm autoconf apache2 libxml2 libxml2-dev libssl-dev pkg-config curl libcurl4-nss-dev enchant libenchant-dev libjpeg8 libjpeg8-dev libpng12-0 libpng12-dev libvpx1 libvpx-dev libfreetype6 libfreetype6-dev libt1-5 libt1-dev libgmp10 libgmp-dev libicu-dev mcrypt libmcrypt4 libmcrypt-dev libpspell-dev libedit2 libedit-dev libsnmp-dev libxslt1.1 libxslt1-dev checkinstall autoconf php5-mysql

RUN a2enmod rewrite

# recompile php 5.5.18
RUN wget http://de1.php.net/get/php-5.5.18.tar.gz/from/this/mirror -O php-5.5.18.tar.gz && tar -xzvf php-5.5.18.tar.gz
WORKDIR /php-5.5.18
RUN ./configure --enable-debug --enable-maintainer-zts --enable-sockets --enable-pthreads --with-mysqli=mysqlnd && make && checkinstall && cp php.ini-production /usr/local/lib/php.ini

RUN pear config-set php_ini /usr/local/lib/php.ini
RUN pecl config-set php_ini /usr/local/lib/php.ini
RUN pecl install pthreads

ADD install-ebot.sh /tmp/install-ebot.sh
RUN chmod 777 /tmp/install-ebot.sh
RUN /tmp/install-ebot.sh

ADD config/config.ini /home/ebot/ebot-csgo/config/config.ini

ADD start-ebot.sh /tmp/start-ebot.sh
RUN chmod 777 /tmp/start-ebot.sh
RUN /tmp/start-ebot.sh

EXPOSE 80

CMD ["/usr/sbin/apache2ctl", "-D", "FOREGROUND"]
