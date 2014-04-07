(ns clj-123.core
  (:require 
   [clojure.java.io :as io])
  (:import java.util.UUID
	   javax.crypto.Mac
	   javax.crypto.spec.SecretKeySpec
	   org.apache.commons.codec.binary.Hex
           java.lang.Runtime))


(declare add-smime-header tidy-enc)

(defn- hash_hmac_sha1 [msg key]
  (Hex/encodeHexString
   (.doFinal (doto
		 (Mac/getInstance "HmacSHA1")
	       (.init (SecretKeySpec. (.getBytes key) "HmacSHA1")))
	     (.getBytes msg))))


(defn make-123 [openssl private-key public-key]
  ;; create 123 component
  {:openssl openssl
   :private-key private-key
   :public-key public-key})


(defn encrypt
  [ott pxml]
  (let [{:keys [openssl private-key public-key]} ott
        input-file (str (.toString (UUID/randomUUID)) ".txt")
	output-file (str (.toString (UUID/randomUUID)) ".txt")
        shell-cmd (str openssl " smime -encrypt -in " input-file " -out " output-file " " public-key)]
    (with-open [wrtr (io/writer input-file)]
      (.write wrtr pxml))
    (try
      (let [proc (. (Runtime/getRuntime) exec shell-cmd)
            ret (.waitFor proc)]
        (if (zero? ret)
          (clojure.string/join "" (subvec (clojure.string/split (slurp output-file) #"\n") 5))
          (throw (ex-info "Unable to encrypt: "
                          {:type :openssl-encryption :command shell-cmd :error-message (slurp (.getErrorStream proc))}))))
      (catch Exception e (throw e))
      (finally (do
                 (io/delete-file input-file true)
                 (io/delete-file output-file true))))))


(defn decrypt
  [ott p]
  (let [{:keys [openssl private-key public-key]} ott
        input-file (str (.toString (UUID/randomUUID)) ".txt")
	output-file (str (.toString (UUID/randomUUID)) ".txt")
        shell-cmd (str openssl " smime -decrypt -in " input-file " -out "  output-file " -recip " private-key)]
    (with-open [wrtr (io/writer input-file)]
      (.write wrtr (add-smime-header (tidy-enc p))))
    (try 
      (let [proc (. (Runtime/getRuntime) exec shell-cmd)
            ret (.waitFor proc)]
        (if (zero? ret)
          (slurp output-file)
          (throw (ex-info "Unable to encrypt: "
                          {:type :openssl-encryption :command shell-cmd :error-message (slurp (.getErrorStream proc))}))))
      (catch Exception e (throw e))
      (finally (do
                 (io/delete-file input-file true)
                 (io/delete-file output-file true))))))
      


(defn- add-smime-header [s]
  (str "MIME-Version: 1.0\nContent-Disposition: attachment; filename=\"smime.p7m\"\nContent-Type: application/x-pkcs7-mime; smime-type=enveloped-data; name=\"smime.p7m\"\nContent-Transfer-Encoding: base64\n\n" s))


(defn- tidy-enc [st]
  (clojure.string/join
   "\n"
   (re-seq #".{0,64}" st)))


  
