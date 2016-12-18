(defproject search-blogs "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.5.1"]
                 [ring/ring-json "0.4.0"]
                 [cheshire "5.6.3"]
                 [hiccup "1.0.5"]
                 [http-kit "2.2.0"]
                 [prismatic/plumbing "0.5.3"]
                 [org.clojure/data.xml "0.0.8"]
                 [ring/ring-jetty-adapter "1.5.0" :exclusions [ring/ring-core]]]
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler search-blogs.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}}
  :main search-blogs.handler)
