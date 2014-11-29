(ns varjocafe.views
  (:require [net.cgrand.enlive-html :as en]))

(en/deftemplate layout "templates/layout.html"
                [{:keys [title]}]
                [:title] (en/content title)
                [:h1] (en/content title))

(defn main-page []
  (layout {:title "VarjoCafe"}))
