(ns esport-parser.collector
  (:require [clojure.tools.logging :as log]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [clojurewerkz.meltdown.reactor :as mr]
            [clojurewerkz.meltdown.selectors :refer  [R]]
            )
  (:use esport-parser.schema)
  (:use esport-parser.cassandra)
  (:use esport-parser.team)
  (:use esport-parser.utils)
  (:use esport-parser.round)
  (:use esport-parser.game)
  (:import [java.util Date]))

;; :dispatcher-type :event-loop
(def reactor (mr/create :event-routing-strategy :broadcast
                        ))



(defn roundSpawn [event]
  (let [server (getEventItem event :server)
        line (getEventItem event :line) 
        game (getGame server)
        ]
    (if game 
      (gameEnd server game {} nil)
      )
    ))

(defn matchRoundend [line]
  (re-find #"^(.*) \"(.*)\".*\"(.*)\" \W{1}(\w{1,2}) \"(\w{1,2})\".*\W{1}(\w{1,2}) \"(\w{1,2})\"" line)
  )

(defn round_or_game_ended [event]
  (try
    (let [server (getEventItem event :server)
          line (getEventItem event :line) 
          game (getGame server)
          round (getRound server (get game :id))
          ]
      (if round
        (do 
          (log/info "Round or game ended. " line)
          (let [matches  (matchRoundend line)
                winner (teamShort  (get matches 2))
                points {(.toLowerCase (get matches 4)) (get matches 5) (.toLowerCase (get matches 6)) (get matches 7)}
                ]
            (log/info "Points " points " Winner:" winner)
            (handleRoundEnd round server winner points)
            (checkIfgameEnd server game points winner)
            ))

        (log/info "Round/Game ended " game " Round:" round))
      )
    (catch Exception e (log/error e (.getMessage e)))))



(defn collect [message]
  (try (insertEvent (get message :server) (get message :line))
    (catch Exception e (log/fatal (.getMessage e))))

  (try
    (let [line (get message :line)
          server (get message :server)]
      (log/info "::: Line: " line)
      (mr/notify reactor line {:line line :server server})

      )
  (catch Exception e (log/error e (.getMessage e))))

)
(defn collector_init [] 
  (mr/on reactor (R ".*World triggered \"Round_Start\".*") round_started )
  (mr/on reactor (R ".*Restart_Round_.*") game_started )
  (mr/on reactor (R ".*SFUI_Notice.*") round_or_game_ended )
  (mr/on reactor (R ".*mp_teamname_.*") add_team )

  ;; eBot triggered "Round_Spawn"
  ) 
