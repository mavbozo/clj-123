(ns clj-123.core
  (:require 
   [clojure.java.io :as io]
   [clojure.data.xml :refer [sexp-as-element emit indent-str]]
   [clj-time core format])
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


  
(declare invoice-format recipe number-format request-template hash-hmac-sha1 timestamp-format multiple-line-text-format payment-items-schema)


(defn make-config [MerchantID APIKey ProviderPublicKey MerchantPrivateKey]
  {:MerchantID MerchantID :APIKey APIKey :ProviderPublicKey ProviderPublicKey :MerchantPrivateKey MerchantPrivateKey})



(defn make-request [cnf data]
  (let [base (-> (merge-with #(%1 %2) recipe data)
                 (assoc :MerchantID (:MerchantID cnf)))
        hash (hash_hmac_sha1 (apply str ((juxt :MerchantID :Amount) base)) (:APIKey cnf))]
    (->> (assoc base :HashValue hash)
         (map (fn [e] 
                (if (coll? (val e))
                  (into [(key e) {}] (val e))
                  (conj [(key e) {}] (val e)))))
         (into [:OneTwoThreeReq {}])
         sexp-as-element)))



(def recipe (apply array-map 
                   [:Version identity
                    :TimeStamp timestamp-format
                    :MessageID identity
                    :MerchantID identity
                    :InvoiceNo invoice-format
                    :Amount number-format
                    :Discount number-format
                    :ServiceFee number-format
                    :ShippingFee number-format
                    :CurrencyCode identity
                    :CountryCode identity
                    :ProductDesc identity
                    :PaymentItems payment-items-schema
                    :PayerName identity
                    :ShippingAddress identity
                    :MerchantUrl identity
                    :APICallUrl identity
                    :AgentCode identity
                    :ChannelCode identity
                    :PayInSlipInfo multiple-line-text-format
                    :UserDefined1 identity
                    :UserDefined2 identity
                    :UserDefined3 identity
                    :UserDefined4 identity
                    :UserDefined5 identity
                    :HashValue identity]))


(defn invoice-format [invoice-no]
  (format "%012d" invoice-no))

(defn number-format [num]
  (str (format "%010d" num) "00"))

(def ^:private request-template 
   (sexp-as-element
     [:OneTwoThreeReq {} 
        [:Version {} "1.1"]
        [:TimeStamp {} "TimeStamp"]
        [:MessageID {} "MessageID"]
        [:MerchantID {} "MerchantID"]
        [:InvoiceNo {} "InvoiceNo"]
        [:Amount {} "Amount"]
        [:Discount {} "Discount"]
        [:ServiceFee {} "ServiceFee"]
        [:ShippingFee {} "ShippingFee"]
        [:CurrencyCode {} "IDR"]
        [:CountryCode {} "IDN"]
        [:ProductDesc {} "ProductDesc"]
        [:PaymentItems {}
         [:PaymentItem {:id "id" :name "" :price ""}]]
         ;;[:PaymentItem {:id "18" :name "VE131 - Voucher Elektrik 1 Bulan" :price (str (format "%010d" 150000) "00") :quantity 1}]]
        [:PayerName {} "PayerName"]
        [:PayerEmail {} "PayerEmail"]
        [:ShippingAddress {} "ShippingAddress"]
        [:MerchantUrl {} "MerchantUrl"]
        [:APICallUrl {} "APICallUrl"]
        [:AgentCode {} "AgentCode"]
        [:ChannelCode {} "ChannelCode"]
        [:PayInSlipInfo {} " "]
        [:UserDefined1 {} " "]
        [:UserDefined2 {} " "]
        [:UserDefined3 {} " "]
        [:UserDefined4 {} " "]
        [:UserDefined5 {} " "]
        [:HashValue {} "HashValue"]]))


(defmacro ^:private request-template-keys []
  '[Version
    TimeStamp
    MessageID
    MerchantID
    InvoiceNo
    Amount
    Discount
    ServiceFee
    ShippingFee
    CurrencyCode
    CountryCode
    ProductDesc
    PaymentItems
    PayerName
    ShippingAddress
    MerchantUrl
    APICallUrl
    AgentCode
    ChannelCode
    PayInSlipInfo
    UserDefined1
    UserDefined2
    UserDefined3
    UserDefined4
    UserDefined5])


(def ^:private custom-123-date-formatter 
  (clj-time.format/formatter "yyyy-MM-dd HH:mm:ss:SSS"))


(defn ^:private timestamp-format [t]
  ;; t in UTC
  (clj-time.format/unparse custom-123-date-formatter t))


(defn ^:private multiple-line-text-format [text]
  ;; text has at most 5 lines and each line has at most 100 characters not including newline char
  ;; else this function will truncate that text
  (->> text
       (partition 3)
       (map #(apply str %))
       (map clojure.string/split-lines)
       flatten
       (filter (complement clojure.string/blank?))
       (take 5)
       (clojure.string/join "|")))


(defn ^:private payment-items-schema [coll]
  (->> coll
       (map (fn [x] (-> x
                        (update-in [:price] number-format)
                        (assoc :id (:sku x))
                        (dissoc :sku)
                        (list)
                        (conj :PaymentItem)
                        vec)))))
