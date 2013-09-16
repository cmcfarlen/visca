(ns visca.core
  (:require [clojure.string :as string]
            [clojure.core.async :as async :refer :all])
  (:import [gnu.io CommPortIdentifier CommPort SerialPort]
           [java.util UUID]
           [java.nio ByteBuffer]))

(defn port-names 
  "sequence of port names"
  []
  (map #(.getName %) (enumeration-seq (CommPortIdentifier/getPortIdentifiers))))

(defrecord Port [name ident port in out])

(defn open
  "Open a port by name"
  [name]
  (let [portid (CommPortIdentifier/getPortIdentifier name)
        n      (UUID/randomUUID)
        serial (.open portid (.toString n) 5000)]
    (Port. name portid serial (.getInputStream serial) (.getOutputStream serial))))

(defmulti write (fn [_ what] (class what)))

(defmethod write String [port s]
  (.write (:out port) (.getBytes s)))

(defmethod write java.nio.ByteBuffer [port buff]
  (.write (:out port) (.array buff)))

(defn read-byte [port]
  (let [b (.read (:in port))]
    (bit-and b 0xff)))

(defn close
  "Close the port"
  [port]
  (let [in (:in port)
        out (:out port)]
    (.close in)
    (.close out)
    (.close (:port port))))


(defn encode [msg]
  (let [buff (ByteBuffer/allocate (count msg))]
    (->> msg
        (map unchecked-byte)
        (byte-array))))

(defn write-command [port cmd]
  (let [ba (encode cmd)]
    (.write (:out port) ba)
    port))

(defn read-response [port]
  (loop [n (read-byte port)
         msg []]
    (if (= n 0xff)
      (conj msg n)
      (recur (read-byte port) (conj msg n)))))

(defn command [port cmd]
  (do (write-command port cmd)
      (read-response port)))

(defn dump-message [msg]
  (string/join " " (map (partial format "%02x") msg)))

(defn move-up [spd]
  [0x81 0x01 0x06 0x01 spd spd 0x03 0x01 0xff])

(defn move-down [spd]
  [0x81 0x01 0x06 0x01 spd spd 0x03 0x02 0xff])

(defn move-left [spd]
  [0x81 0x01 0x06 0x01 spd spd 0x01 0x03 0xff])

(defn move-right [spd]
  [0x81 0x01 0x06 0x01 spd spd 0x02 0x03 0xff])

(defn move-stop []
  [0x81 0x01 0x06 0x01 0 0 0x3 0x3 0xff])

(def IF_Address [0x88 0x30 0x01 0xff]) 
(def IF_Clear [0x81 0x01 0x00 0x01 0xff]) 

(defn -main
  [& args]
  (let [p (open (first args))]
    (print (dump-message (command p IF_Address)))))

; (port-names)
;
;
; (encode [ 0x90 0x01 0x02 0xff ])
;
; (let [p (open "/dev/tty.PL2303-0000151D")] (dump-message (command p [0x88 0x30 0x01 0xff])))
;       
;
