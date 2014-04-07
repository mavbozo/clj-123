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



(def dummy-data {:Version "1.1"
                 :TimeStamp (clj-time.core/now)
                 :MessageID "UUID"
                 :MerchantID "merchant@zenius.net"
                 :InvoiceNo 123
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
                 :MerchantUrl "//zenius.net"
                 :APICallUrl "//zenius.net"
                 :AgentCode "INDOMARET"
                 :ChannelCode "OVERTHECOUNTER"
                 :PayInSlipInfo " "
                 :UserDefined1 " "
                 :UserDefined2 " "
                 :UserDefined3 " "
                 :UserDefined4 " "
                 :UserDefined5 " "
                 :HashValue " "})


