(ns varjocafe.views
  (:require [net.cgrand.enlive-html :as en]))

(en/defsnippet restaurant-row "templates/layout.html" [:.restaurant-row]
               [restaurant]
               [[:.restaurant-name]] (en/content (:name restaurant)))

(en/deftemplate layout "templates/layout.html"
                [{:keys [data]}]
                [:.restaurant-row] (en/substitute (map #(restaurant-row %) (vals data)))
                [:#data-dump] (en/content data))

(defn main-page [data]
  (layout {:data data}))
