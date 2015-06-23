(ns esport-parser.players)

(def terrorists (ref {}))

(def cops (ref {}))

(defn update-player-team [player old new]
  (println (str "Player: " player))
  (println (str "old: " old))
  (println (str "new: " new))
  (case old
    "<TERRORIST>" (dosync (alter @terrorists dissoc player))
    "<CT>" (dosync (alter @cops dissoc player))
    "default")
  (case new
    "<TERRORIST>" (dosync (alter terrorists assoc player player))
    "CT" (dosync (alter cops assoc player player))
    "default")
  (println (str "Terrorists: " @terrorists))
  (println (str "Cops: " @cops)))

(defn update-teams [line]
  ;;(re-find #"<\D+>" "10:37:41: \"Zunnu<3><STEAM_1:0:42115274>\" switched from team <Unassigned> to <TERRORIST>")
  (let [player (re-find #"\w+" (re-find #":\D+" line))            ;; : \"Zunnu< --> Zunnu
        groups (re-find #"<\D+>" line)                            ;; <Unassigned> to <TERRORIST>
        from-group (re-find #"<\S+>" groups)                      ;; <Unassigned>
        to-group (re-find #"<\S+>" (re-find #"to <\S+>" groups))] ;; to <TERRORIST> --> <TERRORIST>
    (update-player-team player from-group to-group)))