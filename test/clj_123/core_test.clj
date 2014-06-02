(ns clj-123.core-test
  (:require 
   [clojure.edn]
   [clj-http.client :as client]
   [clojure.test :refer :all]
   [clj-123.core :refer :all]))

(def ott (clojure.edn/read-string (slurp "config.edn")))

(defn uuid [] 
  (str (-> (java.util.UUID/randomUUID)
           (.toString)
           (clojure.string/replace #"-" ""))))

(def dummy-inquiry-data {:TimeStamp (clj-time.core/now)
                         :MessageID (uuid)
                         :InvoiceNo 15945
                         :Amount 150000
                         :RefNo1 "062616"
                         :UserDefined1 " "
                         :UserDefined2 " "
                         :UserDefined3 " "
                         :UserDefined4 " "
                         :UserDefined5 " "})

(def dummy-data {:TimeStamp (clj-time.core/now)
                 :MessageID (uuid)
                 :InvoiceNo (rand-int 10000)
                 :Amount 10000
                 :Discount 100
                 :ServiceFee 50
                 :ShippingFee 50
                 :CurrencyCode "IDR"
                 :CountryCode "IDN"
                 :ProductDesc "Voucher zenius.net"
                 :PaymentItems [{:sku "V1" :name "Voucher 1" :price 100000 :quantity 1} 
                                {:sku "V2" :name "Voucher 2" :price 200000 :quantity 2}
                                {:sku "V3" :name "Voucher 3" :price 300000 :quantity 3}]
                 :PayerName "Avicenna"
                 :ShippingAddress "JL Batu"
                 :MerchantUrl "https://www.zenius.net"
                 :APICallUrl "https://www.zenius.net"
                 :AgentCode "INDOMARET"
                 :ChannelCode "OVERTHECOUNTER"
                 :PayInSlipInfo " "
                 :UserDefined1 " "
                 :UserDefined2 " "
                 :UserDefined3 " "
                 :UserDefined4 " "
                 :UserDefined5 " "
                 :HashValue " "})

(defn inquire []
  (let [xml-sd (make-inquiry-as-xml-data ott dummy-inquiry-data)
        xml-str (xml-data->ott-xml-string)
        encrypted-xml-str (encrypt ott xml-str)]
    (->> (client/post "https://secure.satuduatiga.co.id/Payment/inquiryapi.aspx"
                      {:debug false 
                       :headers {"Referer" "https://www.zenius.net/"}
                       :form-params {:InquiryReq encrypted-xml-str}})
         identity
         :body
         (decrypt ott)
         (parse-xml-str)
         (parse-inquiry-response)
         )))

(defn request []
  (let [xml-dx (make-request-as-xml-data ott dummy-data)
        xml-str (xml-data->ott-xml-string)
        enc-xml (encrypt ott xml-str)]
    (->> (client/post "http://uat.123.co.th/payment/paywith123.aspx"
                      {:debug false 
                       :headers {"Referer" "https://www.zenius.net/"}})
         :body
         (spit "res.html"))))
