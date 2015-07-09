(ns esport-parser.cassandra
  (:require [clojurewerkz.cassaforte.client :as cc]
            [clojurewerkz.cassaforte.cql :as cql]
            [clojurewerkz.cassaforte.uuids :as uuids]
            [clojurewerkz.cassaforte.query :refer :all]
            [clojure.tools.trace :refer [trace]]
            [clojure.tools.logging :as log]
            [clj-time.core :as t]
            [clj-time.coerce :as c])
  (:use esport-parser.schema)
  )

(defn timeNow [] (c/to-long (t/now)))

(defn db-host []
  (or (System/getenv "CASSANDRA_PORT_9042_TCP_ADDR") "dockerhost"))

(defn db-port [] (read-string (or (System/getenv "CASSANDRA_PORT_9042_TCP_PORT") "44003")))

(def connection (cc/connect [(db-host)] {:port (db-port) :keyspace "esports_keyspace"}))


(defn endGame [id server winner loser]
  (cql/update connection "games"
              {:ended (c/to-long (t/now)) :state "ended" :winner winner :loser loser}
              (where [[= :id id] [= :server server]])))

(defn filterStarted [data] (= (get data :state) "started"))

(defn getLastGame [server]
  (first (filter filterStarted (cql/select connection "games"
                                           (where [[= :server server]])
                                           (order-by [:started :desc])
                                           (limit 1)))))

(defn addNewGame [server game]
  (log/info "Add new Game:" game)
  (cql/insert connection "games" (merge game {:id (uuids/random) :server server})))

(defn updateGame [game]
  (log/info "Game to update: " game)
  (try
    (let [game2upd (dissoc game :id :server :started)]
      (log/info "Game to update: " game2upd)
      (cql/update connection "games" game2upd (where [[= :id (get game :id)]
                                                      [= :server (get game :server)]
                                                      [= :started (get game :started)]
                                                      ]))
      )
    (catch Exception e (log/error e (.getMessage e))))
  )

(defn getAllGames []
  ;;  (order-by [:started :desc])
  (cql/select connection "games"))


(defn getLastRound [gameid]
  (first (filter filterStarted (cql/select connection "rounds"
                                           (where [[= :game gameid]])
                                           (order-by [:started :desc])
                                           (limit 1)))))

(defn addNewRound [server round]
  (log/info "Add new Round:" round)
  (cql/insert connection "rounds" (merge round {:id (uuids/random) :server server})))

(defn updateRound [round]
  (log/info "Update round " round)
  (let [round2upd (dissoc round :id :server :started :game)]
    (cql/update connection "rounds" round2upd (where [[= :id (get round :id)]
                                                      [= :game (get round :game)]
                                                      [= :started (get round :started)]
                                                      [= :server (get round :server)]
                                                      ]))
    )
  )

(defn getAllRounds [gameid]
  (if gameid
    (cql/select connection "rounds" (where [[= :game (java.util.UUID/fromString gameid)]]))
    (cql/select connection "rounds"))
  )



(defn insertEvent [server line]
  (cql/insert connection "events" {:id (uuids/random) :server server :line line :eventtime (c/to-long (t/now))}))



(defn addNewTeam [team]
  (log/info "Add new Team :" team)
  (cql/insert connection "teams" team)
  )

(defn updateTeam [team]
  (log/info "Update team" team)
  (let [team2upd (dissoc team :name :game)]
    (log/info "Team to update: " team2upd)
    (try
      (cql/update connection "teams" team2upd (where [[= :name (get team :name)]
                                                      [= :game (get team :game)]]
                                                     )
                  )
      (catch Exception e (log/error e (.getMessage e)))
      )))

(defn getTeam [teamName server game]
  (first (cql/select connection "teams" (where [[= :name teamName] [= :game (get game :id)]])
                     )))

(defn upsertTeam [server team game]
  (log/info "Team to upsert: " team game)
  (try
    (let [orgTeam (getTeam (get team :name) server game)]
      (log/info "Upsert team " team " found " orgTeam)
      (if (not-empty orgTeam)
        (updateTeam team)
        (addNewTeam team)
        )
      )
    (catch Exception e (log/error e (.getMessage e))))
  )

(defn getAllTeams [gameid]
  (if gameid
    (cql/select connection "teams" (where [[= :game (java.util.UUID/fromString gameid)]]))
    (cql/select connection "teams")))


(defn getPlayer [server player]
  (first (cql/select connection "players" (where [[= :steamid (get player :steamid)] [= :server server]]))))

(defn addNewPlayer [player]
  (cql/insert connection "players" player))

(defn updatePlayer [player]
  (let [player2upd (dissoc player :steamid :server)]
    (log/info "Update " player ":-:" player2upd ":" (get player :steamid))
    (cql/update connection "players" player2upd (where [[= :steamid (get player :steamid)]
                                                        [= :server (get player :server)]]
                                                       ))
    )
  )

(defn upsertPlayer [server player]
  (let [orgPlayer (getPlayer server player)
        ]
    (log/info "Upsert player " player " found " orgPlayer)
    (try
      (if (not-empty orgPlayer)
        (updatePlayer player)
        (addNewPlayer player)
        )

      (catch Exception e (log/error e (.getMessage e))))
    ))

(defn getAllPlayers [server]
  (cql/select connection "players" (where [[= :server server]]) (allow-filtering true))
  )

(defn updateNewRoundPlayer [game round player updateParams]
  (cql/update connection "round_players" updateParams (where [[= :game (get game :id)]
                                                              [= :round (get round :id)]
                                                              [= :player_name (get player :name)]]))
  )

(defn getRoundPlayers [game]
  (cql/select connection "round_players" (where [[= :game (get game :id)]]) (allow-filtering true)))

(defn getRoundPlayer [game round player]
  (first (cql/select connection "round_players" (where [[= :game (get game :id)]
                                                        [= :round (get round :id)]
                                                        [= :player_name (get player :name)]]) (allow-filtering true))))