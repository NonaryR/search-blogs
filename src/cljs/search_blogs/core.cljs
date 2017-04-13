(ns search-blogs.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [ajax.core :refer [POST]]
              [clojure.string :as str]))

(defonce state (atom {}))

;; -------------------------
;; Actions

(defn error-handler [{:keys [status status-text]}]
  (println "ERR:" status status-text))

(defn list-handler [w-list]
  (reset! state
    (into (array-map) w-list)))

(defn add-words [words]
  (let [word {:words words}]
    (POST "/search"
      {:params word
       :format :json
       :response-format :json
       :keywords? true
       :handler list-handler
       :error-handler error-handler})))

(defn valid-words? [text]
  (not (str/blank? (str/trim text))))

;; -------------------------
;; Views

(defn nav []
  [:nav.navbar.navbar-default
   [:div.container-fluid
    [:div.navbar-header
     [:a.navbar-brand "Поиск по блогам"]]
    [:ul.nav.navbar-nav.navbar-right
     [:li [:a {:href "/"} "Поиск"]]
     [:li [:a {:href "/about"} "О задаче"]]]]])

(defn words-text [new-words]
  [:input#desc.form-control
   {:type "text"
    :placeholder "clojure, scala, python"
    :size "50"
    :value @new-words
    :on-change #(reset! new-words (-> % .-target .-value))}])

(defn submit-button [new-words message]
  [:button.btn.btn-primary
   {:type "submit"
    :style {"marginLeft" "10px"}
    :on-click #(do
                 (reset! message "")
                 (if (valid-words? @new-words)
                   (do
                     (add-words @new-words)
                     (reset! new-words ""))
                   (reset! message "Запрос не может быть пустым.")))}
   [:span.glyphicon.glyphicon-search]])

(defn error-display [message]
  [:div#message
   {:style {"color" "red"}}
   @message])

(defn table []
  [:table.table.table-striped
   [:thead [:tr [:th "Домен"] [:th "Число упоминаний"]]]
    [:tbody
     (map (fn [[k v]] [:tr [:td k] [:td v]]) @state)]])

(defn home-page []
  (let [new-words (atom "")
        message (atom "")]
    [:div
     [nav]
     [:div#main.container
      [:h2 "Введите слова для поиска"]
      [:div#add.form-inline
       [:label {:for "desc", :style {"marginRight" "10px"}}
        "Слова:"]
       [words-text new-words]
       [submit-button new-words message]
       [table]]
      [error-display message]]]))

(defn about-page []
  [:div
   [nav]
   [:div.container
    [:h2 "Задача"]
    [:div
     [:p "Необходимо разработать законченное веб-приложение, реализующее следующую функцию:"]
     [:ul
      [:li [:p "Обслуживать HTTP запросы по URL \"/search\"."]]
      [:li [:p "Сервис должен обраться к API Яндекса поиска по форумам и блогам, по HTTP, получить ответ через RSS."
            [:br]
            " В случае, если ключевых слов переданно больше одного, запросы должны выполняться параллельно (по одному HTTP запросу на ключевое слово). "
            [:br]
            "Должно быть ограничение на максимальное количество одновременных HTTP-соединений, это значение нельзя превышать."
            [:br]
            "Если ключевых слов больше, нужно организовать очередь обработки так, чтобы более указанного количество соединений не открывалось. "
            [:br]
            "По каждому слову ищем только первые 10 записей."]]
      [:li [:p "Из каждого результата извлекаем основную ссылку (поле link)."
            [:br]
            "Из ссылки берем hostname, из которого берется только домен второго уровня"]]
      [:li [:p "В результате работы веб-сервиса должна быть возвращена статистика по доменам второго уровня - сколько раз в сумме использовался домен по всем ключевым словам"
            [:br]
            "В случае, если по разным ключевым словам было найдены полностью идентичные ссылки, хост должен учитываться только один раз"]]
      [:li [:p "Результат должен быть предствлен в формате JSON."
            [:br]
            "Выдача ответа с человеко-читаемым форматированием (pretty print) будет рассматриваться как плюс."]]]]]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/about" []
  (session/put! :current-page #'about-page))

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (secretary/dispatch! path))
     :path-exists?
     (fn [path]
       (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (mount-root))
