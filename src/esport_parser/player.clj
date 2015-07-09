(ns esport-parser.player
  (:require [clojure.tools.logging :as log]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [clojurewerkz.cassaforte.query :refer :all]
            [clojure.stacktrace :refer [print-cause-trace]]
            )
  (:use esport-parser.cassandra)
  (:use esport-parser.utils)
  (:use esport-parser.team)
  (:use esport-parser.game)
  (:import [java.util Date])
  )


;; Player events followed when there is a ongoing game.
;; '" entered the game' -> player
;; ' connected, address ' -> player
;; '" purchased "' -> player
;; '" switched from team ' -> player
;; '" assisted killing "' -> player
;; ' killed "' -> player
;; ' attacked "' -> player


;; Make sure that player have a team and it exists
(defn upsertPlayerCheck [name steamid server side game round]
  (try
    (let [player {:name name :steamid steamid :server server :team (get (getTeamWithside server side) :name)}]
      (do
        (upsertPlayer server player)
        (when (and game round)
          (updateNewRoundPlayer game round player {:died (increment-by 0)}))))
    (catch Exception e (log/fatal (print-cause-trace e))))
  )

(defn purchaseMatch [line] (re-find #".*^(.*) \"(.*)<(.*)><(.*)><(.*)>\".*\"(.*)\"" line))

;; '" purchased "' -> player
;; 12:06:05: "alluu<19><STEAM_1:0:42072674><TERRORIST>" purchased "awp"
(defn purchases [event]
  (let [server (getEventItem event :server)
        line (getEventItem event :line)
        matches (purchaseMatch line)
        game (getGame server)
        round (if game (getRound server (get game :id)))
        playername (get matches 2)
        steamid (get matches 4)
        side (teamShort (get matches 5))
        weapon (get matches 6)
        ]
    (upsertPlayerCheck playername steamid server side game round)
    (log/info "Player" playername " purchased: " weapon)
    )
  )


;; '" switched from team ' -> player
;; 12:05:25: "alluu<19><STEAM_1:0:42072674>" switched from team <CT> to <TERRORIST>
(defn teamswitchmatch [line] (re-find #"^(.*) \"(.*)<(.*)><(.*)\"(.*) <(.*)>.*<(.*)>" line))

(defn teamswitch [event]

  (let [server (getEventItem event :server)
        line (getEventItem event :line)
        game (getGame server)
        round (if game (getRound server (get game :id)))
        matches (teamswitchmatch line)
        playername (get matches 2)
        steamid (get matches 4)
        newside (teamShort (get matches 7))
        oldside (teamShort (get matches 6))
        ]
    (log/info "Player team switch: " playername ":" oldside "->" newside ":" game)
    ;; Player switch from ct -> t or t -> ct
    (if (and oldside game newside)
      (do
        (log/info "Player switching during game")
        (let [_ (upsertPlayerCheck playername steamid server oldside game round)
              player (getPlayer server {:steamid steamid})
              team (getTeam (get player :team) server game)]
          (if team
            (updateTeamSide server team newside)
            )
          )))
    ;; New player first time
    ;; from unassigned to -> t|ct
    (if (and (not oldside) round game newside)
      (do
        (log/info "New Player switching first time")
        (upsertPlayerCheck playername steamid server newside game round)
        ))
    ))

;; '" assisted killing "' -> player
;;
(defn assistKill [event]
  (log/info "Assist kill: " event)
  )



;; 2 killername, 3 steamid, 4 killerside, 6 killedname, 7 killed steam id, 8 killed side, 9 weapon, 11 headshot(optional)
(defn killmatch [line] (re-find #".*^(.*) \"(.*)<(.*)><(.*)>\"(.*)\"(.*)<(.*)><(.*)>\".*\"(.*)\".?(\(?)(\w*)(\)?)" line))

(defn killEvent [event]
  (let [server (getEventItem event :server)
        line (getEventItem event :line)
        game (getGame server)
        round (if game (getRound server (get game :id)))
        ]
    (if (and game round)
      (do
        (let [matches (killmatch line)
              killerName (get matches 2)
              killerSteam (get matches 3)
              killderSide (get matches 4)
              killedName (get matches 6)
              killedSteam (get matches 7)
              killedSide (get matches 8)]
          (upsertPlayerCheck killerName killerSteam server killderSide game round)
          (upsertPlayerCheck killedName killedSteam server killedSide game round))
        (let [matches (killmatch line)
              killerPlayer (getPlayer server {:steamid (get matches 3)})
              killedPlayer (getPlayer server {:steamid (get matches 7)})
              headshot (get matches 11 nil)
              ]
          (updateNewRoundPlayer game round killedPlayer {:died (increment-by 1)})
          (updateNewRoundPlayer game round killerPlayer (merge {:killed (increment-by 1)} (if headshot {:headshot (increment-by 1)} {})))
          )))))

