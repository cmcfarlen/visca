(ns visca.async
  (:require [visca.core :as core]
            [clojure.core.async :as async :refer :all]
            [seesaw.core :as seesaw]))


(defn exchange [port ch]
  (go (let [cmd (<! ch)]
        (>! ch (core/command port cmd)))))

(defn response-pump [port ch]
  (go (while true
        (let [resp (core/read-response port)]
          (println "resp" (core/dump-message resp))
          (>! ch resp)))))

(def curr-key (atom 0))

(defn handle-key-pressed [p e]
  (let [c (.getKeyCode e)]
    (if (not= @curr-key c)
      (do (reset! curr-key c)
        (case (.getKeyCode e)
          37 (core/write-command p (core/move-left 10))
          38 (core/write-command p (core/move-up 10))
          39 (core/write-command p (core/move-right 10))
          40 (core/write-command p (core/move-down 10)))))))

(defn handle-key-released [p e]
  (do
    (core/write-command p (core/move-stop))
    (reset! curr-key 0)))


(defn -main
  [& args]
  (let [p (core/open (first args))]
    (seesaw/invoke-later
      (seesaw/listen 
        (-> (seesaw/frame :title "Hello"
               :content "Hello, Seesaw"
               :on-close :exit
                          )
         seesaw/pack!
         seesaw/show!)
        :key-pressed (partial handle-key-pressed p)
        :key-released (partial handle-key-released p)))))

; (defn -main
;   [& args]
;   (let [p (core/open (first args))
;         ch (chan)]
;     (response-pump p ch)
;     (<!! (go (do
;                (core/write-command p core/IF_Address)
;                (<! ch)
; 
;                (core/write-command p core/IF_Clear)
;                (<! ch)
; 
;                (core/write-command p [ 0x81 0x01 0x06 0x02 0x10 0x10 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0xff ])
;                (<! ch)
;                (<! ch)
; 
;                (core/write-command p [ 0x81 0x01 0x06 0x02 0x10 0x10 0x00 0x02 0x00 0x00 0x00 0x00 0x00 0x00 0xff ])
;                (<! ch)
;                (<! ch)
; 
;                (core/write-command p [ 0x81 0x01 0x06 0x02 0x10 0x10 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0xff ])
;                (<! ch)
;                (<! ch)
;                
;                )))))
; 
