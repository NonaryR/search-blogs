(ns search-blogs.ya
  (:require [clojure.string :as str]
            [org.httpkit.client :as http]
            [clojure.data.xml :as xml]
            [plumbing.core :refer [fn->]]
            [cheshire.core :refer [generate-string]]))

(defn some-tag
  [tag nodes]
  (some #(when (= tag (:tag %)) %) nodes))

;; numdoc=10 - по условию задачи, только 10 записей
(def request-template "http://blogs.yandex.ru/search.rss?numdoc=10&text=")

;; атом для проверки уникальности ссылок
(def unique-links (atom #{}))

(defn items-in-rss
  [response]
  (->> response
       :body
       (xml/parse-str)
       :content
       (some-tag :channel)
       :content
       (filter (fn-> :tag (= :item)))))

(defn second-domain
  [item]
  (let [raw-link (->> item
                      :content
                      (some-tag :link)
                      :content
                      (apply str))]
    (when-not (contains? @unique-links raw-link)
      (swap! unique-links conj raw-link)
      (-> raw-link
          (str/split #"//")
          second
          (str/split #"/")
          first))))

(defn collect-domains
  [response]
  (->> response
       (items-in-rss)
       (map #(second-domain %))
       ;; необходимо для доменов типа "oochaequ.livejournal.com"
       (map #(first (re-find #"(\w+\.(com|ru|org|edu))" %)))
       (into [])))

(defn all-mentions
  [coll]
  ;; 5 - столько запросов обрабатываются одновременно
  (let [queue (partition-all 5 coll)]
    (for [batch queue]
      (let [urls (map #(str request-template (str %)) (flatten batch))
            futures (doall (map http/get urls))]
        (for [resp futures]
          (collect-domains @resp))))))

(defn frequency-stat
  [coll]
  (reset! unique-links #{})
  (-> coll
      all-mentions
      flatten
      frequencies))
