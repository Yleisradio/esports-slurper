(ns esport-parser.player-spec
  (:require [clojure.test :refer :all]
            [clojure.tools.trace :refer [trace]]
            [esport-parser.player :refer :all]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [esport-parser.cassandra :refer :all]
            [esport-parser.team :refer :all]
            [esport-parser.utils :refer :all]
            )
  )

(def line_player1_un2Ct "01/17/2015 - 11:21:44: \"player1<3><STEAM_1:0:38750314>\" switched from team <Unassigned> to <CT>")
(def line_player1_ct2t "01/17/2015 - 11:21:44: \"player1<3><STEAM_1:0:38750314>\" switched from team <CT> to <TERRORIST>")
(def line_un2t "01/17/2015 - 11:21:44: \"player2<4><STEAM_1:0:38750314>\" switched from team <Unassigned> to <TERRORIST>")

(def game {:id (clojurewerkz.cassaforte.uuids/random) :server "player_localhost" :started (c/to-long (t/now)) :state "started"})

(def round {:game (get game :id) :state "started" :started (c/to-long (t/now))})

(defn team1 [server] (get-in @ongoing-teams ["player_localhost" "mp_teamname_1"]))
(defn team2 [server] (get-in @ongoing-teams [server "mp_teamname_2"]))

(defn- add2TeamsAndStartGame [server]
  (do
    (add_team {:data {:line "01/17/2015 - 10:39:29: rcon from \"94.23.30.133:45023\": command \"mp_teamname_1 \"team1\"\"" :server server}})
    (add_team {:data {:line "01/17/2015 - 10:39:29: rcon from \"94.23.30.133:45023\": command \"mp_teamname_2 \"team2\"\"" :server server}})
    (is (= "team1" (get-in @ongoing-teams [server "mp_teamname_1" :name])))
    (is (= "team2" (get-in @ongoing-teams [server "mp_teamname_2" :name])))
    (team_start_game server game)
    ))

(def upsert_stub (constantly []))
(def update_stub (constantly []))
(def getGame_stub (constantly game))
(def getRound_stub (constantly round))
(def getTeam_stub (constantly (get-in @ongoing-teams ["player_localhost" "mp_teamname_1"])))



(deftest player-switch
  (testing "Switch from unassign -> ct -> t"
    (with-redefs [getGame getGame_stub]
      (with-redefs [getRound getRound_stub]
        (with-redefs [upsertPlayer upsert_stub]
          (with-redefs [updateNewRoundPlayer update_stub]
            (with-redefs [upsertTeam upsert_stub]
              (with-redefs [updateTeam update_stub]
                (with-redefs [getTeam getTeam_stub]
                  (add2TeamsAndStartGame "player_localhost")

                  (teamswitch {:data {:line line_player1_un2Ct :server "player_localhost"}})
                  (is (= "team1" (get (getTeamWithside "player_localhost" "ct") :name)))
                  (teamswitch {:data {:line line_player1_ct2t :server "player_localhost"}})
                  (is (= "team1" (get (getTeamWithside "player_localhost" "t") :name)))
                  )))))))
    )
  )