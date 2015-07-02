(ns esport-parser.utils
  (:require [schema.core :as s :include-macros true])
  (:import [java.util Date])

  (:use esport-parser.cassandra)
  )

(defn updateState [state key value]
  (if value
    (dosync
      (alter state assoc key value))))

(defn clearState [state key]
  (dosync
    (alter state dissoc key)
    )
  )


(defn teamShort [team]
  (case (.toUpperCase team)
    "TERRORIST" "t"
    "CT" "ct"
    )
  )

(defn getLoserSide [winner]
  (case (.toLowerCase winner)
    "t" "ct"
    "ct" "t"
    )
  )

(defn getEventItem [event key]
  (get (get event :data) key)
  )
(def ongoing-games (ref {}))

(def ongoing-rounds (ref {}))

(defn getLastAndSafe [server] (updateState ongoing-games server (getLastGame server)))

(defn getLastRoundAndSafe [server gameid] (updateState ongoing-rounds server (getLastRound server gameid)))


(defn getGame [server]
  (or
    (when (contains? @ongoing-games :server) (get @ongoing-games server))
    (do
      (getLastAndSafe server)
      (get @ongoing-games server nil))
    )
  )


(defn getRound [server game]
  (or (when (not-empty @ongoing-rounds) (get @ongoing-rounds server)) ((getLastRoundAndSafe server game) ((get @ongoing-rounds server))))
  )

