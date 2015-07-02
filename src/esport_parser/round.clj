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
  (:use esport-parser.round)
  )

(defn handleRoundEnd [round server winner points]
  (updateRound (merge round {:ended     (timeNow) :state "ended"
                             :winner    winner
                             :loser     (getLoserSide winner)
                             :ct        "123" :t "w234"
                             :ct_points (read-string (get points "ct"))
                             :t_points  (read-string (get points "t"))}))
  (clearState ongoing-rounds server)
  )


(defn round_started [event]
  (let [server (getEventItem event :server)
        line (getEventItem event :line)
        game (getGame server)
        round {:game  (get game :id) :state "started" :started (c/to-long (t/now))}]
    (if-not (nil? game)
      (do
        (log/info "---------- Round start: " event)
        (log/info "Round started Ongoing:" game)
        (addNewRound server round)
        (updateState ongoing-rounds server (getLastRound (get game :id)))))))