(ns esport-parser.collector
  (:require [clojure.tools.logging :as log]
            [clj-time.core :as t]
            [clj-time.coerce :as c])
  (:use esport-parser.schema)
  (:use esport-parser.cassandra)
  (:import [java.util Date]))


(def ongoing-games (ref {}))

(def ongoing-rounds  (ref  {}))

(defn updateState  [state key value]
  (if value
    (dosync
      (alter state assoc key  value ))))

(defn clearState [state key]
  (dosync
    (alter state dissoc key )
    )
  )

(defn getLastAndSafe [server] (updateState ongoing-games server  (getLastGame server)))

(defn getLastRoundAndSafe [server gameid] (updateState ongoing-rounds server  (getLastRound server gameid)) )

(defn getGame [server]
  (or (when (not-empty @ongoing-games) (get @ongoing-games server)) ((getLastAndSafe server) ((get @ongoing-games server))))
  )

(defn getRound [server game]
  (or  (when  (not-empty @ongoing-rounds)  (get @ongoing-rounds server))  ((getLastRoundAndSafe server game)  ((get @ongoing-rounds server))))
  )

(defn matchRoundend [line]
  (re-find #"^(.*) \"(.*)\".*\"(.*)\" \W{1}(\w{1,2}) \"(\w{1,2})\".*\W{1}(\w{1,2}) \"(\w{1,2})\"" line)
  )

(defn teamShort [team]
  (case (.toUpperCase team)
    "TERRORIST" "t"
    "CT" "ct"
    )
  )

(defn getLoserTeam [winner]
  (case (.toLowerCase winner)
    "t" "ct"
    "ct" "t"
    )
  )

(defn round_or_game_ended [server line]
  (try
    (log/info "Round or game ended. " line)
    (let [game (getGame server)
          round (getRound server (get game :id))
          ]
      (if round
        (do 
          (let [matches  (matchRoundend line)
                winner (teamShort  (get matches 2))
                points {(.toLowerCase (get matches 4)) (get matches 5) (.toLowerCase (get matches 6)) (get matches 7)}
                ]
            (log/info "Points " points " Winner:" winner)

            (updateRound (merge round {:ended (timeNow) :state "ended" 
                                       :winner winner 
                                       :loser (getLoserTeam winner) 
                                       :ct "123" :t "w234" 
                                       :ct_points (read-string (get points "ct")) 
                                       :t_points (read-string (get points "t")) }))
            (clearState ongoing-rounds server)))
        (log/info "Round/Game ended " game " Round:" round))
      )
    (catch Exception e (log/error e (.getMessage e)))))

(defn game_started [server line]
  (log/info "################ Game started " line)
  (let [game {:server server :started (c/to-long (t/now)) :state "started"}]
    (addNewGame server game))
  (updateState ongoing-games server (getLastGame server)))


(defn round_started [server line]
  (log/info "---------- Round start: " line)
  (let [game (getGame server)
        round {:game  (get game :id) :state "started" :started (c/to-long (t/now))}]
    (if-not (nil? game)
      (do
        (log/info "Round started Ongoing:" game)
        (addNewRound server round)
        (updateState ongoing-rounds server (getLastRound server (get game :id)))))))


(defn player_killed [server line tokens]
  (let [game (getGame server)
        round (getRound server (get game :id))]
      (log/info "player killed round " round))
  (log/info "player killed " (get tokens 8)))

;; (\d{2}:\d{2}:\d{2}:)\s(.*)\s(\".*\")(.*)
;; 12:06:15: World triggered "Restart_Round_(3_seconds)"
;; 12:06:33: World triggered "Round_Start"
(def start-expn #"^(\d{2}:\d{2}:\d{2}:)\s(.*)\s(\".*\")(.*)")

;; player kills (token 7 'kills') (\d{2}:\d{2}:\d{2}:) "(.*)<(.*)><(.*)><(.*)>" (.*) (.*) "(.*)<(.*)><(.*)><(.*)>" (.*) (\w*) "(.*)"\s?(\(*.*\)*)
;;  12:05:50: "b* DogC)<28><STEAM_1:1:29151561><CT>" [-433 -84 -109] killed "Haalis<29><STEAM_1:0:40671441><TERRORIST>" [1008 261 -94] with "m4a1_silencer"
;;  12:05:40: "Boom-say<20><STEAM_1:1:37250067><TERRORIST>" [1001 313 -158] killed "b* DogC)<28><STEAM_1:1:29151561><CT>" [203 266 -95] with "ak47" (headshot)
(def kills-expn #"^\s?(\d{2}\/\d{2}\/\d{4} - \d{2}:\d{2}:\d{2}): \"(.*)<(.*)><(.*)><(.*)>\" (.*) (.*) \"(.*)<(.*)><(.*)><(.*)>\" (.*) (\w*) \"(.*)\"\s?(\(*.*\)*)")

;;
(def notice-expn #"^")

(defn collect [message]
  (try (insertEvent (get message :server) (get message :line))
    (catch Exception e (log/fatal (.getMessage e))))

  (try
    (let [line (get message :line)
          server (get message :server)]

      ;; (\d{2}:\d{2}:\d{2}:)\s(.*)\s(\".*\")(.*)
      (if (.contains line "Restart_Round_") (game_started server line))
      (if (.contains line "World triggered \"Round_Start\"") (round_started server line))
      ;;
      (if (.contains line "SFUI_Notice") (round_or_game_ended server line))

      ;; player kills (token 7 'kills') (\d{2}:\d{2}:\d{2}:) "(.*)<(.*)><(.*)><(.*)>" (.*) (.*) "(.*)<(.*)><(.*)><(.*)>" (.*) (\w*) "(.*)"\s?(\(*.*\)*)
      ;;  12:05:50: "b* DogC)<28><STEAM_1:1:29151561><CT>" [-433 -84 -109] killed "Haalis<29><STEAM_1:0:40671441><TERRORIST>" [1008 261 -94] with "m4a1_silencer"
      ;;  12:05:40: "Boom-say<20><STEAM_1:1:37250067><TERRORIST>" [1001 313 -158] killed "b* DogC)<28><STEAM_1:1:29151561><CT>" [203 266 -95] with "ak47" (headshot)
      ;;(if (.contains line " killed ")
      ;;  (log/info ":" line ":")
      ;;  (let [tokens (re-find kills-expn line)
      ;;      method (get tokens 7)]
      ;;  (if (= "killed" method) (player_killed line tokens))))

      ;; puchase
      )
  (catch Exception e (log/error e (.getMessage e))))

)
