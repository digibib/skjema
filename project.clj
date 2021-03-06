(defproject askjema "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :source-paths ["src/clj"]
  :resources-path "resources"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.5"]
                 [ring-server "0.2.7"]
                 [fogus/ring-edn "0.2.0-SNAPSHOT"]
                 [clj-http "0.6.4"]
                 [cheshire "5.0.1"]
                 [matsu "0.1.3-SNAPSHOT"]
                 [ring-basic-authentication "1.0.2"]
                 [prismatic/dommy "0.0.2"]]
  :plugins [[lein-ring "0.8.3"]
            [lein-cljsbuild "0.3.0"]]
  :ring {:handler askjema.handler/war-handler }
  :immutant {:context-path "/skjema"}
  :profiles {:production
             {:ring
              {:open-browser? false, :stacktraces? false, :auto-reload? false}}
             :dev
             {:dependencies [[ring-mock "0.1.3"] [ring/ring-devel "1.1.8"]]}}
  :cljsbuild {:builds
              [{:source-paths ["src/cljs"],
                :compiler
                {:pretty-print false,
                 :output-to "resources/public/js/app.js",
                 :optimizations :advanced}}]})
