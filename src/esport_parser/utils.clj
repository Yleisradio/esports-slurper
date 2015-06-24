(ns esport-parser.utils
       (:require  [schema.core :as s :include-macros true])
         (:import  [java.util Date]))

(defn updateState  [state key value]
  (if value
    (dosync
      (alter state assoc key  value ))))

(defn clearState  [state key]
  (dosync
    (alter state dissoc key )
    )
  )
