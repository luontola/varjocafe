(ns varjocafe.view
  (:require [net.cgrand.enlive-html :as html]
            [varjocafe.core :as core]
            [varjocafe.format :as format]))

(html/defsnippet date-cell "templates/layout.html" [:.date-column]
                 [date today]
                 [:.date html/any-node] (html/replace-vars {:date (format/date date)})
                 [:.date] (if (= date today)
                            (html/add-class "today")
                            identity)
                 [:.date-column] (html/after "\n        "))

(html/defsnippet food-line "templates/layout.html" [:.food]
                 [food]
                 [:.food] (html/content (format/food-html food))
                 [:.food] (html/after "\n            "))

(html/defsnippet menu-cell "templates/layout.html" [:.restaurant-row.expanded :.menu]
                 [restaurant date]
                 [:.food] (html/clone-for [food (get-in restaurant [:menu date :data])]
                                          (html/substitute (food-line food)))
                 [:.menu] (html/after "\n        "))

(html/defsnippet opening-times-entry "templates/layout.html" [:.restaurant-row.expanded :.opening-times :> html/any-node]
                 [title times]
                 [:dt html/any-node] (html/replace-vars {:title title})
                 [:dd] (html/content times))

(defn opening-times-for-category [restaurant category]
  (let [title (format/opening-times-title restaurant category)
        times (format/opening-times-html restaurant category)]
    (when times
      (opening-times-entry title times))))

(html/defsnippet opening-times "templates/layout.html" [:.restaurant-row.expanded :.opening-times]
                 [restaurant]
                 [:.opening-times] (html/content (->> [:business :lounas :bistro]
                                                      (map #(opening-times-for-category restaurant %)))))

(html/defsnippet restaurant-row "templates/layout.html" [:.restaurant-row]
                 [restaurant dates]
                 [:.restaurant-row] (html/set-attr :data-restaurant-id (:id restaurant))
                 [:.restaurant-name html/any-node] (html/replace-vars {:restaurant-name (:name restaurant)})
                 [:.collapsed :.menu] (html/clone-for [date dates]
                                                      (html/after "\n        "))
                 [:.expanded :.menu] (html/clone-for [date dates]
                                                     (html/substitute (menu-cell restaurant date)))
                 [:.expanded :.opening-times] (html/substitute (opening-times restaurant))
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
                  [:.date-column] (html/clone-for [date dates]
                                                  (html/substitute (date-cell date today)))
                  [:.restaurant-row] nil                    ; will be inserted by area-restaurants
                  [:.area-row.collapsed] nil
                  [:.area-row] (html/clone-for [area areadata]
                                               (html/substitute (area-restaurants area dates))))

(defn main-page [data today settings]
  (let [in-past? #(< (compare % today) 0)]
    (layout {:dates    (->> (core/dates data)
                            (drop-while in-past?)
                            (take 2))
             :today    today
             :areadata (core/restaurants-by-area data (:areacode-names settings))})))
