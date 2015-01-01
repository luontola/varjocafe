(ns varjocafe.view
  (:import (java.util Locale)
           (org.joda.time.format DateTimeFormat DateTimeFormatter))
  (:require [net.cgrand.enlive-html :as html]
            [varjocafe.core :as core]
            [clojure.string :as string]))

(def date-format (-> (DateTimeFormat/forPattern "E d.M.")
                     (.withLocale (Locale/forLanguageTag "fi"))))

(html/defsnippet date-cell "templates/layout.html" [:.date]
                 [date today]
                 [:.date] (html/content (.print date-format date))
                 [:.date] (if (= date today)
                            (html/transform-content (html/wrap "span" {:class "today"}))
                            identity)
                 [:.date] (html/after "\n        "))

(defn format-allergens [food]
  (let [allergens (->> food
                       :meta
                       (sort-by first)
                       vals
                       flatten)]
    (if (empty? allergens)
      nil
      (html/html " " [:span.allergens (str "(" (string/join ", " allergens) ")")]))))

(defn format-food [food]
  (html/html (:name food) (format-allergens food)))

(html/defsnippet food-line "templates/layout.html" [:.food]
                 [food]
                 [:.food] (html/content (format-food food))
                 [:.food] (html/after "\n            "))

(html/defsnippet menu-cell "templates/layout.html" [:.menu]
                 [restaurant date]
                 [:.food] (html/clone-for [food (get-in restaurant [:menu date :data])]
                                          (html/substitute (food-line food)))
                 [:.menu] (html/after "\n        "))

(html/defsnippet restaurant-row "templates/layout.html" [:.restaurant-row]
                 [restaurant dates]
                 [:.restaurant-row] (html/set-attr :data-restaurant-id (:id restaurant))
                 [:.restaurant-name html/any-node] (html/replace-vars {:restaurant-name (:name restaurant)})
                 [:.collapsed :.restaurant-name] (html/set-attr :colspan (+ 1 (count dates)))
                 [:.menu] (html/clone-for [date dates]
                                          (html/substitute (menu-cell restaurant date)))
                 [:.restaurant-row] (html/after "\n"))

(html/defsnippet area-restaurants "templates/layout.html" [#{:.area-row :.restaurant-row}]
                 [area dates]
                 [:.area-row] (html/set-attr :data-area-id (:areacode area))
                 [:.area-name] (html/set-attr :colspan (+ 1 (count dates)))
                 [:.area-name html/any-node] (html/replace-vars {:area-name (:name area)})
                 [:.restaurant-row.collapsed] nil
                 [:.restaurant-row] (html/clone-for [restaurant (:restaurants area)]
                                                    (html/substitute (restaurant-row restaurant dates)))
                 [:.restaurant-row] (html/before "\n    ")
                 [:.area-row] (html/before "\n    "))

(html/deftemplate layout "templates/layout.html"
                  [{:keys [dates today areadata]}]
                  [:.date] (html/clone-for [date dates]
                                           (html/substitute (date-cell date today)))
                  [:.restaurant-row] nil                    ; will be inserted by area-restaurants
                  [:.area-row] (html/clone-for [area areadata]
                                               (html/substitute (area-restaurants area dates))))

(defn main-page [data today settings]
  (let [in-past? #(< (compare % today) 0)]
    (layout {:dates    (->> (core/dates data)
                            (drop-while in-past?)
                            (take 2))
             :today    today
             :areadata (core/restaurants-by-area data (:areacode-names settings))})))
