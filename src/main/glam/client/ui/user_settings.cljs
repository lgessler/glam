(ns glam.client.ui.user-settings
  (:require [com.fulcrologic.fulcro.components :as c :refer [defsc]]
            [glam.client.router :as r]
            [glam.models.session :as sn]))

(defsc UserSettings [this {:as props}]
  {:ident (fn [_] [:component/id :user-settings])
   :query [sn/session-join]
   :initial-state {}
   :route-segment (r/route-segment :user-settings)}
  (when (sn/valid-session? props)
    [:div "test"]))