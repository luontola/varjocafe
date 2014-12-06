(ns varjocafe.view
  (:require [net.cgrand.enlive-html :as en]
            [varjocafe.core :as core]))

(en/defsnippet date-cell "templates/layout.html" [:.date]
               [date]
               [[:.date]] (en/content (str date)))

(en/defsnippet menu-cell "templates/layout.html" [:.menu]
               [restaurant date]
               [[:.menu]] (en/content (->> (get-in restaurant [:menu date :data])
                                           (map :name)
                                           (clojure.string/join ", "))))

(en/defsnippet restaurant-row "templates/layout.html" [:.restaurant-row]
               [restaurant dates]
               [[:.restaurant-name]] (en/content (:name restaurant))
               [[:.menu]] (en/substitute (map #(menu-cell restaurant %) dates)))

(en/deftemplate layout "templates/layout.html"
                [{:keys [data dates]}]
                [:.date] (en/substitute (map #(date-cell %) dates))
                [:.restaurant-row] (en/substitute (map #(restaurant-row % dates)
                                                       (vals data)))
                [:#data-dump] (en/content data))

(defn main-page [data]
  (layout {:data  data
           :dates (take 5 (core/dates data))}))
