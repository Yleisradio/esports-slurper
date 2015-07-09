(ns esport-parser.team
  (:require [clojure.tools.logging :as log]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [esport-parser.cassandra :refer [upsertTeam updateTeam]]
            [esport-parser.utils :refer :all]
            )
  )


(def ongoing-teams (ref {}))

(defn updateOngoingTeam [server team]
  (let [mergedTeam (merge (get (get @ongoing-teams server {}) (get team :id)) team)
        team2update (assoc (get @ongoing-teams server {}) (get team :id) mergedTeam)]
    (updateState ongoing-teams server team2update)
    )
  )

;; "^(.*) \"(.*) \"(.*)\"\""
(defn matchTeamName [line]
  (re-find #"^(.*) \"(.*) \"(.*)\"\"" line)
  )

;; get current team with side(t,ct) and game
(defn getTeamWithside [server side]
  (log/info "getTeamWithside" side ":" (get @ongoing-teams server))
  (let [teamentry (first (filter (fn [team]
                                   (= (get (val team) :side) side)) (get @ongoing-teams server)))
        team (when teamentry (val teamentry))]
    team
    ))

(defn teaminitside [teamid]
  (case teamid
    "mp_teamname_1" "ct"
    "mp_teamname_2" "t"
    )
  )

(defn updateTeamSide [server team newside]
  (log/info "updateTeamSide:" team newside)
  (updateTeam (assoc team :side newside))
  (updateOngoingTeam server (assoc team :side newside))
  )

(defn updateTeamPoints [server team points]
  (log/info "updateTeamPoints:" team points)
  (updateTeam (assoc team :points (int points)))
  (updateOngoingTeam server (assoc team :points (int points)))
  )

;; add new current team from the log
(defn add_team [event]
  (let [line (getEventItem event :line)
        server (getEventItem event :server)
        tokens (matchTeamName line)
        teamId (get tokens 2)
        teamName (get tokens 3)
        team {:name teamName :id teamId :side (teaminitside teamId)}
        ]
    (log/info "Team " teamId ":" teamName ":" team)
    (updateOngoingTeam server team)
    )
  )

;; set team_1 to be ct and team 2 to be t 
;; insert to db current teams from this server. 
(defn team_start_game [server game]
  (log/info "Teams:" @ongoing-teams " " server " " game)
  (log/info (get @ongoing-teams server))
  (doseq [[id team] (get @ongoing-teams server)]
    (if (and team game)
      (do
        (log/info "Team to upsert" team)
        (upsertTeam server (assoc team :game (get game :id)) game)
        (updateOngoingTeam server (assoc team :game (get game :id)))
        )))
  )

;; end game and clean teams.
(defn team_end_game [server game]
  (log/info "Teams end game")
  (clearState ongoing-teams server)
  )

(defn add_player [server player])