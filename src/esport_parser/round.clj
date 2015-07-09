(ns esport-parser.round
  (:require [clojure.tools.logging :as log]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [clojurewerkz.meltdown.reactor :as mr]
            [clojurewerkz.meltdown.selectors :refer [R]]
            )
  (:use esport-parser.schema)
  (:use esport-parser.cassandra)
  (:use esport-parser.team)
  (:use esport-parser.utils)
  )

(defn handleRoundEnd [round server winner points]

  (updateRound (merge round {:ended     (timeNow) :state "ended"
                             :winner    (get (getTeamWithside server winner) :name)
                             :loser     (get (getTeamWithside server (getOtherSide winner)) :name)
                             :ct        (get (getTeamWithside server "ct") :name)
                             :t         (get (getTeamWithside server "t") :name)
                             :ct_points (read-string (get points "ct"))
                             :t_points  (read-string (get points "t"))}))
  (updateTeamPoints server (getTeamWithside server "ct") (read-string (get points "ct")))
  (updateTeamPoints server (getTeamWithside server "t") (read-string (get points "t")))
  (clearState ongoing-rounds server))




(defn round_started [event]
  (let [server (getEventItem event :server)
        line (getEventItem event :line)
        game (getGame server)
        round {:game (get game :id) :state "started" :started (c/to-long (t/now))}]
    (if-not (nil? game)
      (do
        (log/info "---------- Round start: " event)
        (log/info "Round started Ongoing:" game)
        (addNewRound server round)
        (updateState ongoing-rounds server (getLastRound (get game :id)))))))