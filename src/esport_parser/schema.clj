(ns esport-parser.schema
  (:require [schema.core :as s :include-macros true])
  (:import [java.util Date]))

(s/defschema Player {:id Integer
                     :name String
                     (s/optional-key :score) Integer
                     (s/optional-key :assists) Integer
                     (s/optional-key :deaths) Integer
                     (s/optional-key :headshots) Integer
                     (s/optional-key :kills) Integer
                     :steamId String})

(s/defschema Team {:name String
                   (s/optional-key :side) [(s/enum :CT :T)]
                   (s/optional-key :players) [Player]})

(s/defschema Round {:id Integer
                    :state [(s/enum :started :ended)]
                    :start Date
                    (s/optional-key :end) Date
                    (s/optional-key :teams) [Team]})


(s/defschema Game {:id Integer
                   :start Date
                   (s/optional-key :end) Date
                   :state [(s/enum :started :ended)]
                   :server String
                   (s/optional-key :teams) [Team]
                   (s/optional-key :rounds) [Round]})


