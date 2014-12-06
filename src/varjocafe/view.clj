(ns varjocafe.view
  (:require [net.cgrand.enlive-html :as en]
            [varjocafe.core :as core]))

(en/defsnippet date-cell "templates/layout.html" [:.date]
               [date]
               [[:.date]] (en/content (str date)))

(en/defsnippet restaurant-row "templates/layout.html" [:.restaurant-row]
               [restaurant]
               [[:.restaurant-name]] (en/content (:name restaurant))
               [[:.menu]] (en/content (first (vals (:menu restaurant)))))

(en/deftemplate layout "templates/layout.html"
                [{:keys [data]}]
                [:.date] (en/substitute (map #(date-cell %) (core/dates data)))
                [:.restaurant-row] (en/substitute (map #(restaurant-row %) (vals data)))
                [:#data-dump] (en/content data))

(defn main-page [data]
  (layout {:data data}))
