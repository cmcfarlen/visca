(ns visca.async
  (:require [visca.core :as core]
            [clojure.core.async :as async :refer :all]))


(defn exchange [port ch]
  (go (let [cmd (<! ch)]
        (>! ch (core/command port cmd)))))

(defn response-pump [port ch]
  (go (while true
        (let [resp (core/read-response port)]
          (println "resp" (core/dump-message resp))
          (>! ch resp)))))

(defn -main
  [& args]
  (let [p (core/open (first args))
        ch (chan)]
    (response-pump p ch)
    (<!! (go (do
               (core/write-command p core/IF_Address)
               (<! ch)

               (core/write-command p core/IF_Clear)
               (<! ch)

               (core/write-command p [ 0x81 0x01 0x06 0x02 0x10 0x10 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0xff ])
               (<! ch)
               (<! ch)

               (core/write-command p [ 0x81 0x01 0x06 0x02 0x10 0x10 0x00 0x02 0x00 0x00 0x00 0x00 0x00 0x00 0xff ])
               (<! ch)
               (<! ch)

               (core/write-command p [ 0x81 0x01 0x06 0x02 0x10 0x10 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0xff ])
               (<! ch)
               (<! ch)
               
               )))))

