(ns varjocafe.view
  (:require [net.cgrand.enlive-html :as en]
            [varjocafe.core :as core]))

(en/defsnippet date-cell "templates/layout.html" [:.date]
               [date]
               [[:.date]] (en/content (str date)))

(en/defsnippet food-line "templates/layout.html" [:.food]
               [food]
               [[:.food]] (en/content (:name food)))

(en/defsnippet menu-cell "templates/layout.html" [:.menu]
               [restaurant date]
               [[:.food]] (en/substitute (map #(food-line %) (get-in restaurant [:menu date :data]))))

(en/defsnippet restaurant-row "templates/layout.html" [:.restaurant-row]
               [restaurant dates]
               [[:.restaurant-name]] (en/content (:name restaurant))
               [[:.menu]] (en/substitute (map #(menu-cell restaurant %) dates)))

(en/defsnippet area-row "templates/layout.html" [:.area-row]
               [area]
               [[:.area-name]] (en/content (:name area)))

(en/deftemplate layout "templates/layout.html"
                [{:keys [data dates areadata]}]
                [:.date] (en/substitute (map #(date-cell %) dates))
                [:.area-row] (en/substitute (map #(area-row %)
                                                 areadata))
                [:.restaurant-row] (en/substitute (map #(restaurant-row % dates)
                                                       (vals data))))

(defn main-page [data settings]
  (layout {:data     data
           :dates    (take 5 (core/dates data))
           :areadata (core/restaurants-by-area data (:areacode-names settings))}))
