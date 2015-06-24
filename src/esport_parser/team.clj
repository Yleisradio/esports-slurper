(ns esport-parser.team
  (:require  [clojure.tools.logging :as log]
            [clj-time.core :as t]
            [clj-time.coerce :as c])
  (:use esport-parser.schema)
  (:use esport-parser.cassandra)
  (:use esport-parser.utils)
  (:import  [java.util Date])
  )


(def ongoing-teams (ref {}))


;; "^(.*) \"(.*) \"(.*)\"\""
(defn matchTeamName  [line]
  (re-find #"^(.*) \"(.*) \"(.*)\"\"" line)
  )

;; get current team with side(t,ct) and game
(defn get_team [server side game])

(defn teaminitside [teamid]
 (case teamid
   "mp_teamname_1" "ct"
   "mp_teamname_2" "t"
   ) 
  )

;; add new current team from the log
(defn add_team [server line]
  (let [tokens (matchTeamName line)
        teamId (get tokens 2)
        teamName (get tokens 3)
        team {:name teamName :id teamId :side (teaminitside teamId)}
        ]
    (log/info "Team " teamId ":" teamName)
    (updateState ongoing-teams (str server teamId) team)

    )
  )

;; set team_1 to be ct and team 2 to be t 
;; insert to db current teams from this server. 
(defn start_game [server game]
  (log/info "Teams:" @ongoing-teams)
  ;; (addNewTeam server {:name })
  )

;; end game and clean teams.
(defn end_game [server game]
  (log/info "Teams end game")
  )

(defn add_player [server player])
