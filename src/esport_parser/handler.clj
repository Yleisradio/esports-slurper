(ns esport-parser.handler
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]
            [clojure.tools.logging :as log]
            [esport-parser.server :as server])
  (:use esport-parser.schema)
  (:use esport-parser.cassandra)
  (:use esport-parser.collector))

(defn init [] (do
                (collector_init)
                (server/start 8081)
                ))

(defn destroy [] (server/stop))

(defapi app
        (swagger-ui)
        (swagger-docs
          {:info {:title       "Esport-parser"
                  :description "Compojure Api example"}
           :tags [{:name "hello", :description "says hello in Finnish"}]})
        (context* "/api" []
                  :tags ["Games"]
                  (GET* "/listgames" []
                        :summary "List games"
                        (ok (getAllGames)))
                  (GET* "/listrounds/:gameid" []
                        :summary "List rounds"
                        :path-params [gameid :- String]
                        (ok (getAllRounds gameid)))
                  (GET* "/listteams/:gameid" []
                        :summary "List rounds"
                        :path-params [gameid :- String]
                        (ok (getAllTeams gameid)))
                  (GET* "/listroundplayers/:gameid" []
                        :summary "List players of the game/rounds"
                        :path-params [gameid :- String]
                        (ok (getRoundPlayers {:id (java.util.UUID/fromString gameid)})))
                  (GET* "/listplayers/:server" []
                        :summary "List all players from the server"
                        :path-params [server :- String]
                        (ok (getAllPlayers server)))
                  ))
