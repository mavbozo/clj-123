(ns user
  "Tools for interactive development with the REPL. This file should
  not be included in a production build of the application."
  (:require 
   [clojure.tools.namespace.repl :refer (refresh refresh-all)]
   [clj-123.core :refer :all]
   [clojure.test]))


(defn run-tests []
  (clojure.test/run-tests 'clj-123.core-test))
        
(defn reset []
  (refresh :after 'user/run-tests))
