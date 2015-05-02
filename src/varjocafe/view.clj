(ns varjocafe.view
  (:require [net.cgrand.enlive-html :as html]
            [varjocafe.core :as core]
            [varjocafe.format :as format])
  (:import (org.joda.time LocalDate DateTimeConstants)))

(def compile-step #'net.cgrand.enlive-html/compile-step)

(defn whitespace? [s]
  (and (string? s)
       (re-matches #"\s*" s)))

(defn indent-of [selector-step]
  (let [selector (compile-step selector-step)]
    (fn [current]
      (let [next (clojure.zip/right current)]
        (boolean (and (whitespace? (clojure.zip/node current))
                      next
                      (selector next)))))))

(defn indented [selector-step]
  (html/union [(compile-step selector-step)
               (indent-of selector-step)]))

(defn refine [selector transformation]
  (fn [nodes] (html/at nodes selector transformation)))

(html/defsnippet date-cell "templates/layout.html" [(indented :.date-column)]
                 [date today]
                 [:.date html/any-node] (html/replace-vars {:date (format/date date)})
                 [:.date] (if (= date today)
                            (html/add-class "today")
                            identity)
                 [:.date-column] (html/set-attr :data-date (str date)))

(html/defsnippet exception-line "templates/layout.html" [(indented :.exception)]
                 [restaurant exception]
                 [:.exception html/any-node] (html/replace-vars
                                               {:exception (format/exception restaurant exception)}))

(html/defsnippet food-line "templates/layout.html" [(indented :.food)]
                 [food]
                 [:.allergens] (let [allergens (format/allergens food)]
                                 (if (empty? allergens)
                                   nil
                                   (refine [html/any-node] (html/replace-vars {:allergens allergens}))))
                 [:.food html/any-node] (html/replace-vars {:food  (:name food)
                                                            :price (:name (:price food))}))

(html/defsnippet collapsed-menu-cell "templates/layout.html" [:.restaurant-row.collapsed (indented :.menu)]
                 [])

(html/defsnippet menu-cell "templates/layout.html" [:.restaurant-row.expanded (indented :.menu)]
                 [restaurant date]
                 [(indent-of :.exception)] nil
                 [:.exception] (html/clone-for [exception (core/exceptions-for-date restaurant date)]
                                               (html/substitute (exception-line restaurant exception)))
                 [(indent-of :.food)] nil
                 [:.food] (html/clone-for [food (get-in restaurant [:menu date :data])]
                                          (html/substitute (food-line food)))
                 [:.menu] (html/set-attr :data-date (str date)))

(html/defsnippet opening-times-entry "templates/layout.html" [:.opening-times #{(indented :dt) (indented :dd)}]
                 [title times]
                 [:dt html/any-node] (html/replace-vars {:title title})
                 [:dd] (html/content times))

(defn opening-times-for-category [restaurant category]
  (let [title (format/opening-times-title restaurant category)
        times (format/opening-times-html restaurant category)]
    (when times
      (opening-times-entry title times))))

(html/defsnippet opening-times "templates/layout.html" [:.opening-times]
                 [restaurant]
                 [:.opening-times #{(indented :dt) (indented :dd)}] nil ; keep only the whitespace before </dl>
                 [:.opening-times] (html/prepend (->> (keys core/opening-time-categories)
                                                      (map #(opening-times-for-category restaurant %)))))

(html/defsnippet restaurant-row "templates/layout.html" [(indented :.restaurant-row)]
                 [restaurant dates]
                 [:.restaurant-row] (html/set-attr :data-restaurant-id (:id restaurant))
                 [:.restaurant-name html/any-node] (html/replace-vars {:restaurant-name (:name restaurant)})
                 [:.restaurant-address html/any-node] (html/replace-vars {:restaurant-address (format/restaurant-address restaurant)})
                 [:.collapsed (indent-of :.menu)] nil
                 [:.collapsed :.menu] (html/clone-for [date dates]
                                                      (html/substitute (collapsed-menu-cell)))
                 [:.expanded (indent-of :.menu)] nil
                 [:.expanded :.menu] (html/clone-for [date dates]
                                                     (html/substitute (menu-cell restaurant date)))
                 [:.expanded :.opening-times] (html/substitute (opening-times restaurant)))

(html/defsnippet area-restaurants "templates/layout.html" [#{(indented :.area-row) :.restaurant-row}]
                 [area dates]
                 [:.area-row] (html/set-attr :data-area-id (:areacode area))
                 [:.area-name] (html/set-attr :colspan (+ 1 (count dates)))
                 [:.area-name html/any-node] (html/replace-vars {:area-name (:name area)})
                 [:.restaurant-row.collapsed] nil
                 [:.restaurant-row] (html/clone-for [restaurant (:restaurants area)]
                                                    (html/substitute (restaurant-row restaurant dates))))

(html/defsnippet google-analytics "templates/google-analytics.html" [:script]
                 [{{tracking-id :tracking-id} :google-analytics}]
                 [:script] (if tracking-id
                             identity
                             nil)
                 [:script html/any-node] (html/replace-vars {:tracking-id tracking-id}))

(defn- pad-if [snippet padding]
  (if (empty? snippet)
    snippet
    (html/html snippet padding)))

(html/deftemplate layout "templates/layout.html"
                  [{:keys [dates today areadata settings]}]
                  [(indent-of :.date-column)] nil
                  [:.date-column] (html/clone-for [date dates]
                                                  (html/substitute (date-cell date today)))
                  [(indented :.restaurant-row)] nil         ; will be inserted by area-restaurants
                  [(indent-of :.area-row)] nil
                  [:.area-row.collapsed] nil
                  [:.area-row] (html/clone-for [area areadata]
                                               (html/substitute (area-restaurants area dates)))
                  [:body] (html/append (pad-if (google-analytics settings) "\n\n")))

(defn main-page [data dates today settings]
  (layout {:dates    dates
           :today    today
           :areadata (core/restaurants-by-area data (:areacode-names settings))
           :settings settings}))

(defn- first-day-of-week [^LocalDate date]
  (if (= DateTimeConstants/MONDAY (.getDayOfWeek date))
    date
    (recur (.minusDays date 1))))

(defn- week-of [^LocalDate date]
  (let [start (first-day-of-week date)]
    (for [offset (range 7)]
      (.plusDays start offset))))

(defn next-week [^LocalDate today]
  (week-of (.plusDays today 7)))

(defn this-week [^LocalDate today]
  (week-of today))

(defn today-and-tomorrow [^LocalDate today]
  [today (.plusDays today 1)])
