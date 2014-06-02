(ns clj-123.core
  (:require 
   [clojure.java.io :as io]
   [clojure.data.xml :refer [sexp-as-element emit indent-str parse]]
   [clj-time core format]
   [clojure.pprint])
  (:import java.util.UUID
	   javax.crypto.Mac
	   javax.crypto.spec.SecretKeySpec
	   [javax.xml.bind DatatypeConverter]
           java.lang.Runtime))


(declare add-smime-header tidy-enc invoice-format request-recipe inquiry-recipe number->ott-number request-template timestamp-format multiple-line-text-format payment-items-schema ott-hash-hmac-sha1)


(defn make-123 
    "Make 123 component
    Public functions that have ott as first parameter requires
    a map that looks like this:
     {:merchant-id your merchant id as given by 123
      :api-key your api key as given by 123
      :openssl location of your openssl program
      :private-key location of your private key given by 123
      :public-key  location of 123 public key given by 123}
    ex.
    {:merchant-id \"merchant@mywebsite.com\",
     :api-key \"THEQUICKBROWNFOXJUMPSOVERTHELAZYDOG\",
     :openssl \"C:/OpenSSL-Win64/bin/openssl.exe\",
     :private-key \"resources/certs/MyPrivateKey(123).pem\",
     :public-key \"resources/certs/123PublicKey.pem\"}"
    [{:keys [merchant-id api-key openssl private-key public-key]}]
    {:merchant-id merchant-id
     :api-key api-key
     :openssl openssl
     :private-key private-key
     :public-key public-key})



(defn make-request-as-xml-data 
  "make 123 request as clojure.data.xml's xml data format"
  [ott req]
  (let [req  (merge req {:Version "1.1"})
        base (-> (merge-with #(%1 %2) request-recipe req)
                 (assoc :MerchantID (:merchant-id ott)))
        hash (ott-hash-hmac-sha1 (apply str ((juxt :MerchantID :Amount) base)) (:api-key ott))]
    (->> (assoc base :HashValue hash)
         (map (fn [e] 
                (if (coll? (val e))
                  (into [(key e) {}] (val e))
                  (conj [(key e) {}] (val e)))))
         (into [:OneTwoThreeReq {}])
         sexp-as-element)))


(defn make-inquiry-as-xml-data 
  "make 123 inquiry as clojure.data.xml's xml data format"
  [ott inq]
  (let [inq  (merge inq {:Version "1.1"})
        base (-> (merge-with #(%1 %2) inquiry-recipe inq)
                 (assoc :MerchantID (:merchant-id ott)))
        hash (ott-hash-hmac-sha1 (apply str ((juxt :MerchantID :InvoiceNo :Amount) base)) (:api-key ott))]
    (->> (assoc base :HashValue hash)
         (map (fn [e] 
                (if (coll? (val e))
                  (into [(key e) {}] (val e))
                  (conj [(key e) {}] (val e)))))
         (into [:InquiryReq {}])
         sexp-as-element)))


(defn xml-data->ott-xml-string 
  "return 123 xml string from clojure.data.xml's xml data format"
  [xd]
  (-> (indent-str xd)
      (clojure.string/replace "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" "")))


(defn encrypt
  "encrypt xml string according to 123 encryption rules"
  [ott xml-str]
  (let [{:keys [openssl private-key public-key]} ott
        input-file (str (.toString (UUID/randomUUID)) "-123-openssl-result")
	output-file (str (.toString (UUID/randomUUID)) "-123-openssl-result")
        shell-cmd (str openssl " smime -encrypt -in " input-file " -out " output-file " " public-key)]
    (with-open [wrtr (io/writer input-file)]
      (.write wrtr xml-str))
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
  "decrypt 123 data according to 123 decryption rules to xml string"
  [ott xml-str]
  (let [{:keys [openssl private-key public-key]} ott
        input-file (str (.toString (UUID/randomUUID)) "-123-openssl-result")
	output-file (str (.toString (UUID/randomUUID)) "-123-openssl-result")
        shell-cmd (str openssl " smime -decrypt -in " input-file " -out "  output-file " -recip " private-key)]
    (with-open [wrtr (io/writer input-file)]
      (.write wrtr (add-smime-header (tidy-enc xml-str))))
    (try 
      (let [proc (. (Runtime/getRuntime) exec shell-cmd)
            ret (.waitFor proc)]
        (if (zero? ret)
          (slurp output-file)
          (throw (ex-info "Unable to decrypt: "
                          {:type :openssl-decryption :command shell-cmd :error-message (slurp (.getErrorStream proc))}))))
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


(defn- int->ott-invoice-format 
  "invoice number format for 123 service.
   string of length 12 with 0 left-padding.
   ex: 000000000123.
   invoice-no must be integer"
  [invoice-no]
  (format "%012d" invoice-no))


(defn- number->ott-number 
  "123 format for numerical values such as price.
   string of length 12 with 0 left-padding.
   last 2 chars in the string is for decimals"
  [num]
  (let [a (double num)
        b (format "%.2f" a)
        c (clojure.string/replace b #"\." "")]
    (clojure.pprint/cl-format nil "~12,'0d" c)))


(def ^:private ott-timestamp-formatter
  ;; custom 123 timestamp formatter for use with clj-time
  (clj-time.format/formatter "yyyy-MM-dd HH:mm:ss:SSS"))


(defn ^:private joda-time->ott-timestamp-0 
  "convert joda-time to 123 custom timestamp format
   t in UTC"
  [t]
  (clj-time.format/unparse ott-timestamp-formatter t))


(defn ^:private str->ott-multiple-line-text-format [text]
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
  "payment-items schema for 123"
  (->> coll
       (map (fn [x] (-> x
                        (update-in [:price] number->ott-number)
                        (assoc :id (:sku x))
                        (dissoc :sku)
                        (list)
                        (conj :PaymentItem)
                        vec)))))

(def ^:private request-recipe {:Version identity
                              :TimeStamp joda-time->ott-timestamp-0
                              :MessageID identity
                              :MerchantID identity
                              :InvoiceNo int->ott-invoice-format
                              :Amount number->ott-number
                              :Discount number->ott-number
                              :ServiceFee number->ott-number
                              :ShippingFee number->ott-number
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
                              :PayInSlipInfo str->ott-multiple-line-text-format
                              :UserDefined1 identity
                              :UserDefined2 identity
                              :UserDefined3 identity
                              :UserDefined4 identity
                              :UserDefined5 identity
                              :HashValue identity})

(def ^:private inquiry-recipe {:Version identity
                               :TimeStamp joda-time->ott-timestamp-0
                               :MessageID identity
                               :MerchantID identity
                               :InvoiceNo int->ott-invoice-format
                               :Amount number->ott-number
                               :RefNo1 identity
                               :UserDefined1 identity
                               :UserDefined2 identity
                               :UserDefined3 identity
                               :UserDefined4 identity
                               :UserDefined5 identity
                               :HashValue identity})


(defn- ott-timestamp-0->joda-time 
  "ott-timestamp with microsecond
  ex: 2014-05-31 21:11:05:153"
  [t]
  (clj-time.format/parse ott-timestamp-formatter t))


(defn- mysql-timestamp->joda-time 
  "ex: 2014-05-30 14:26:32"
  [t]
  (clj-time.format/parse (clj-time.format/formatters :mysql) t))


(defn- invoice-number-unformatter [x]
  x)

(defn- number->ott-number-unformatter [x]
  x)

(def ^:private inquiry-response-recipe
  (apply array-map
         [:Version str
          :TimeStamp ott-timestamp-0->joda-time
          :MessageID str
          :MerchantID str
          :InvoiceNo invoice-number-unformatter
          :RefNo1 str
          :ResponseCode str
          :CompletedDateTime mysql-timestamp->joda-time
          :Amount number->ott-number-unformatter
          :PaidAmount number->ott-number-unformatter
          :PayerName str
          :PayerEmail str
          :SelectedAgentCode str
          :SelectedChannelCode str
          :PaymentAgentCode str
          :PaymentChannelCode str
          :ApprovalCode str
          :AgentRef str
          :ChannelService str
          :UserDefined1 str
          :UserDefined2 str
          :UserDefined3 str
          :UserDefined4 str
          :UserDefined5 str
          :HashValue str]))



(defn parse-xml-str [xmls]
  (-> (str "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" xmls)
      (java.io.StringReader.)
      (clojure.data.xml/parse)))

(defn parse-inquiry-response [resp]
  (let [resp-map-seq (map (fn [el]
                        (let [{:keys [tag attrs content]} el]
                          {tag (first content)}))
                      (:content resp))
        resp-map (apply merge resp-map-seq)]
    (merge-with #(%1 %2) inquiry-response-recipe resp-map)))
            

(defn- ott-hash-hmac-sha1 
  "123 hash hmac sha1"
  [msg key]
  (-> (DatatypeConverter/printHexBinary
       (.doFinal (doto
                     (Mac/getInstance "HmacSHA1")
                   (.init (SecretKeySpec. (.getBytes key) "HmacSHA1")))
                 (.getBytes msg)))
      (clojure.string/upper-case)))
