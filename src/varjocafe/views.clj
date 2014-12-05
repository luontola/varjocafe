(ns varjocafe.views
  (:require [net.cgrand.enlive-html :as en]))

(en/deftemplate layout "templates/layout.html"
                [{:keys [data]}]
                [:#content] (en/content data))

(defn main-page [data]
  (layout {:data data}))
