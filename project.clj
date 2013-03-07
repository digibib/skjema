(defproject askjema "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :source-paths ["src/clj"]
  :dependencies [[org.clojure/clojure "1.5.0"]
                 [compojure "1.1.5"]
                 [enlive "1.1.1"]
                 [ring-server "0.2.7"]
                 [fogus/ring-edn "0.2.0-SNAPSHOT"]]
  :plugins [[lein-ring "0.8.3"]
            [lein-cljsbuild "0.3.0"]]
  :ring {:handler askjema.handler/war-handler }
  :profiles {:production
             {:ring
              {:open-browser? false, :stacktraces? false, :auto-reload? false}}
             :dev
             {:dependencies [[ring-mock "0.1.3"] [ring/ring-devel "1.1.8"]]}})
