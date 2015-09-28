(defproject clj-123 "0.1.1-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :codox {:output-dir "../clj-123-doc"}
  :dependencies [[org.clojure/clojure "1.8.0-alpha5"]
                 [commons-codec "1.10"]
                 [clj-time "0.11.0"]
                 [org.clojure/data.xml "0.0.8"]
                 [clj-http "2.0.0"]
                 [org.clojure/test.check "0.8.2"]
                 [prismatic/schema "1.0.1"]]
   :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.10"]]
                   :source-paths ["dev"]}}
   :repl-options {:init-ns user})

