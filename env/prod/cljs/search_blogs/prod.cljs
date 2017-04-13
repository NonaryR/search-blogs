(ns search-blogs.prod
  (:require [search-blogs.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
