FROM java:7
MAINTAINER Jussi Pöri / Yleisradio

ADD target/server.jar /srv/server.jar

EXPOSE 3000

CMD ["java", "-jar", "/srv/server.jar"] 
