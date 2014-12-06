(ns varjocafe.view
  (:require [net.cgrand.enlive-html :as html]
            [varjocafe.core :as core]))

(html/defsnippet date-cell "templates/layout.html" [:.date]
                 [date]
                 [[:.date]] (html/content (str date)))

(html/defsnippet food-line "templates/layout.html" [:.food]
                 [food]
                 [[:.food]] (html/content (:name food)))

(html/defsnippet menu-cell "templates/layout.html" [:.menu]
                 [restaurant date]
                 [[:.food]] (html/substitute (map #(food-line %) (get-in restaurant [:menu date :data]))))

(html/defsnippet restaurant-row "templates/layout.html" [:.restaurant-row]
                 [restaurant dates]
                 [[:.restaurant-name]] (html/content (:name restaurant))
                 [[:.menu]] (html/substitute (map #(menu-cell restaurant %) dates)))

(html/defsnippet area-row "templates/layout.html" [:.area-row]
                 [area]
                 [[:.area-name]] (html/content (:name area)))


(html/deftemplate layout "templates/layout.html"
                  [{:keys [data dates areadata]}]
                  [:.date] (html/substitute (map #(date-cell %) dates))
                  [:.area-row] (html/substitute (map #(area-row %)
                                                     areadata))
                  ; TODO: areas and restaurants must be in groups
                  [:.restaurant-row] (html/substitute (map #(restaurant-row % dates)
                                                           (vals data))))

(defn main-page [data settings]
  (layout {:data     data
           :dates    (take 5 (core/dates data))
           :areadata (core/restaurants-by-area data (:areacode-names settings))}))
