#!/bin/bash

/usr/bin/php5 symfony cc
/usr/bin/php5 symfony doctrine:build --all --no-confirmation
/usr/bin/php5 symfony guard:create-user --is-super-admin admin@ebot admin admin
