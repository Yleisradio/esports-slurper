(ns esport-parser.cassandra
    (:require [clojurewerkz.cassaforte.client :as cc]
            [clojurewerkz.cassaforte.cql    :as cql]
            [clojurewerkz.cassaforte.uuids    :as uuids]
            [clojurewerkz.cassaforte.query    :refer :all]
            [clojure.tools.logging :as log]
            [clj-time.core :as t]
            [clj-time.coerce :as c])
    (:use esport-parser.schema))

(defn timeNow [] (c/to-long  (t/now)))

(def  connection (cc/connect ["dockerhost"] {:port 44003 :keyspace "esports_keyspace"}))

(defn endGame [id server winner loser]
      (cql/update connection "games"
          {:ended (c/to-long (t/now)) :state "ended" :winner winner :loser loser}
          (where [[= :id id][= :server server]])))


(defn endRound [id server t ct t_points ct_points winner loser]
      (cql/update connection "rounds"
          {:ended (c/to-long (t/now)) :state "ended" :winner winner :loser loser :ct ct :t t :ct_points ct_points :t_points t_points}
          (where [[= :id id][= :server server]])))

(defn getLastGame [server]
  (first (cql/select connection "games"
                     (where [[= :server server]])
                     (order-by [:started :desc])
                     (limit 1))))

(defn getLastRound [server]
  (first (cql/select connection "rounds"
                     (where [[= :server server]])
                     (order-by [:started :desc])
                     (limit 1))))


(defn addNewGame [server game]
  (log/info "Add new Game:" game)
  (cql/insert connection "games" (merge game {:id (uuids/random) :server server} )))

(defn addNewRound [server round]
  (log/info "Add new Round:" round)
  (cql/insert connection "rounds" (merge round {:id (uuids/random) :server server} )))


(defn updateGame [game]
  (cql/update connection "games" (dissoc game :id :server :started) (where [[= :id (get game :id)]])))

(defn updateRound [round]
  (cql/update connection "rounds" (dissoc round :id :server :started ) (where [[= :id (get round :id)]]))
  )

(defn getAllGames []
  ;;  (order-by [:started :desc])
  (cql/select connection "games"))

(defn getAllRounds []
  (cql/select connection "rounds")
  )

(defn insertEvent [server line]
  (cql/insert connection "events" {:id  (uuids/random) :server server :line line :eventtime (c/to-long (t/now))}))
