(ns search-blogs.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.adapter.jetty :as jetty]
            [search-blogs.views :as views]
            [compojure.handler :as handler]
            [ring.middleware.json :as middleware])
  (:gen-class))

(defroutes app-routes
  (GET "/" [] (views/home-page))
  (GET "/search" [] (views/submit-word-page))
  (POST "/search" {params :params} (views/add-items-results-page params))
  (route/not-found "Not Found"))

(def app
  (-> (handler/api app-routes)
      (middleware/wrap-json-body)
      (middleware/wrap-json-response)))

(defn -main
  [& [port]]
  (let [port (Integer. (or port (System/getenv "PORT") 5000))]
    (jetty/run-jetty #'app {:port port :join? false})))
