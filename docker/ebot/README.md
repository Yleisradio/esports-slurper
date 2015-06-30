# Installing docker

## For Linux

* Follow the instructions at: https://docs.docker.com/installation/ubuntulinux/

* Make yourself part of the `docker` group, so you can run `docker` commands without sudo (relogin might be needed)

* Add `127.0.0.1 dockerhost` to your `/etc/hosts`

## For Mac OS X

* Use `brew install docker`

* Run `curl http://static.dockerfiles.io/boot2docker-v1.0.1-virtualbox-guest-additions-v4.3.12.iso > ~/.boot2docker/boot2docker.iso`

* Run `VBoxManage sharedfolder add boot2docker-vm -name home -hostpath /Users`

* Run `boot2docker ip` and add `BOOT2DOCKERIP dockerhost` to your `/etc/hosts`
