(ns esport-parser.game
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
  (:import [java.util Date]))

(defn gameEnd [server game points winner]
  (do
    ;; End game, set winner. Not handling specialcases yet.
    (log/info "Ending game: " game)
    (updateGame (merge game {:ended  (timeNow) :state "ended"
                             :loser  (getOtherSide winner)
                             :winner winner
                             }))
    (clearState ongoing-games server)
    (team_end_game server game)
    )
  )

(defn checkIfgameEnd [server game points winner]
  (if (or (= 16 (read-string (get points "ct" "0"))) (= 16 (read-string  (get points "t" "0"))))
    (gameEnd server game points winner)
    )
  )

(defn refreshGame [server]
  (updateState ongoing-games server (getLastGame server)))

(defn game_started [event]
  (let [server (getEventItem event :server)
        line (getEventItem event :line)
        game {:server server :started (c/to-long (t/now)) :state "started"}]
    (do
      (log/info "################ Game started " event)
      (addNewGame server game)
      (updateState ongoing-games server (getLastGame server))
      (team_start_game server (getGame server))
      )))


