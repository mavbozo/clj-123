(defproject clj-123 "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :codox {:output-dir "../clj-123-doc"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [commons-codec "1.5"]
                 [clj-time "0.6.0"]
                 [org.clojure/data.xml "0.0.7"]]
   :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.4"]
                                  [clj-http "0.9.2"]]
                   :source-paths ["dev"]}}
   :repl-options {:init-ns user})

