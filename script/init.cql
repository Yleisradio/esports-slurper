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
  side text,
  player_name text,
  player_side text,
  player_id text,
  died counter,
  killed counter,
  headshot counter,
  PRIMARY KEY (player_name,game,round,side,player_side,player_id)
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
    id text,
    team text,
    steamid text,
    PRIMARY KEY (name,team)
);

CREATE TABLE teams (
    name text,
    label text,
    side text,
    game uudi,
    points int,
    PRIMARY KEY (name,game)
);

CREATE TABLE events (
  id uuid PRIMARY KEY,
  eventtime timestamp,
  line text,
  server text
);
