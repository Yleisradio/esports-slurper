(ns esport-parser.handler
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]
            [clojure.tools.logging :as log]
            [esport-parser.server :as server])
  (:use esport-parser.schema)
  (:use esport-parser.cassandra))

(defn init [] (server/start 8081))

(defn destroy [] (server/stop))

(defapi app
  (swagger-ui)
  (swagger-docs
    {:info {:title "Esport-parser"
            :description "Compojure Api example"}
     :tags [{:name "hello", :description "says hello in Finnish"}]})
  (context* "/api" []
            :tags ["Games"]
            (GET* "/listgames" []
                  :summary "List games"
                  (ok (getAllGames)))
            (GET* "/listrounds/:gameid" []
                 :summary "List rounds"
                  :path-params  [gameid :- String]
                 (ok (getAllRounds gameid)))
            ))
