CREATE KEYSPACE esports_keyspace WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 1};

USE esports_keyspace;

CREATE TABLE games (
  id uuid,
  state text,
  rounds int,
  started timestamp,
  ended timestamp,
  server text,
  loser text,
  winner text,
  PRIMARY KEY (server,started,id)
);

CREATE TABLE rounds (
  id uuid,
  game uuid,
  round int,
  state text,
  started timestamp,
  ended timestamp,
  server text,
  loser text,
  winner text,
  ct text,
  t text,
  ct_points int,
  t_points int,
  PRIMARY KEY (game,started,id,server)
);

CREATE TABLE round_players (
  game uuid,
  round uuid,
  player_name text,
  died counter,
  killed counter,
  headshot counter,
  assists counter,
  PRIMARY KEY (player_name,game,round)
);


CREATE TABLE weapons (
  name text,
  game uuid,
  purchase counter,
  killed counter,
  headshot counter,
  PRIMARY KEY (name,game)
);

CREATE TABLE players (
    name text,
    server text,
    team text,
    steamid text,
    PRIMARY KEY (steamid,server)
);

CREATE TABLE teams (
    name text,
    label text,
    id text,
    side text,
    game uuid,
    points int,
    PRIMARY KEY (game,name)
);

CREATE TABLE events (
  id uuid PRIMARY KEY,
  eventtime timestamp,
  line text,
  server text
);
