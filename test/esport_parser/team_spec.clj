(ns esport-parser.team-spec
  (:require [clojure.test :refer :all]
            [clojure.tools.trace :refer [trace]]
            [esport-parser.team :refer :all]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [esport-parser.cassandra :refer :all]
            [esport-parser.utils :refer :all]
            )
  )

(defn- add2Teams [server]
  (do
    (add_team {:data {:line "01/17/2015 - 10:39:29: rcon from \"94.23.30.133:45023\": command \"mp_teamname_1 \"team1\"\"" :server server}})
    (add_team {:data {:line "01/17/2015 - 10:39:29: rcon from \"94.23.30.133:45023\": command \"mp_teamname_2 \"team2\"\"" :server server}})
    (is (= "team1" (get-in @ongoing-teams [server "mp_teamname_1" :name])))
    (is (= "team2" (get-in @ongoing-teams [server "mp_teamname_2" :name])))
    ))

(deftest testing-team-add
  (testing "Adding team"
    (let [_ (add_team {:data {:line "01/17/2015 - 10:39:29: rcon from \"94.23.30.133:45023\": command \"mp_teamname_1 \"gkfkg\"\"" :server "test1"}})]
      (is (= "gkfkg" (get-in @ongoing-teams ["test1" "mp_teamname_1" :name])))
      )))

(def upsert_stub (constantly []))
(def update_stub (constantly []))

(deftest testing-game-operations
  (let [server "test2"
        game {:id (clojurewerkz.cassaforte.uuids/random) :server server :started (c/to-long (t/now)) :state "started"}
        _ (add2Teams server)]

    (testing "Start game"
      (with-redefs [upsertTeam upsert_stub]
        (team_start_game server game)
        (is (= "team1" (get (getTeamWithside server "ct") :name)))
        (is (= "team2" (get (getTeamWithside server "t") :name)))
        (is (= nil (get (getTeamWithside server "sdfsdf") :name)))
        )
      )
    (testing "Team switch"
      (with-redefs [updateTeam update_stub]
        (with-redefs [upsertTeam upsert_stub]
          (team_start_game server game)
          (is (= "team1" (get (getTeamWithside server "ct") :name)))
          (is (= "team2" (get (getTeamWithside server "t") :name)))
          (updateTeamSide server (getTeamWithside server "ct") "t")
          (is (= "team1" (get (getTeamWithside server "t") :name)))
          )
        ))

    ))


