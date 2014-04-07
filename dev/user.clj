(ns user
  "Tools for interactive development with the REPL. This file should
  not be included in a production build of the application."
  (:require 
   [clojure.tools.namespace.repl :refer (refresh refresh-all)]
   [clj-123.core :refer :all]))


(defn init []
  (make-123 "C:/OpenSSL-Win64/bin/openssl.exe" "resources/certs/MerchantPrivate(123).pem" "resources/certs/MerchantPublic.cer"))


(defn go []
  (let [ott (init)
        msg "dlsahgdslhglsadhgdslahgsakldhsaohoeytlbnoiwreyhgl'hyasgdhaspoghdlgsdh"
        encrypted (encrypt ott msg)
        decrypted (decrypt ott encrypted)]
    (= msg decrypted)))
