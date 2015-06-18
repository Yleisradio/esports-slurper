(ns esport-parser.server
  (:require [esport-parser.collector :as collector]
            [clojure.tools.logging :as log])
  (:import [java.net InetAddress DatagramPacket DatagramSocket]))


(defn socket [port] (DatagramSocket. port))

(defn hashServer [InetSocketAddress]
  (str (.getHostName InetSocketAddress)))

(defn receive
  [^DatagramSocket socket]
  (let [buffer (byte-array 1024)
        packet (DatagramPacket. buffer 1024)]
    (.receive socket packet)
    {:line (String. (.getData packet)
             0 (.getLength packet)) :server (hashServer (.getSocketAddress packet))}))

(defn receive-loop
  [socket f]
  (future (while true (f (receive socket)))))


(defn stop []
  (log/info "statsd udp server: stopping"))


(defn start [port]
  (log/infof "statsd udp server [%s]: starting" port)
  (receive-loop (socket port) collector/collect)
  (log/info "statsd udp server: started"))
