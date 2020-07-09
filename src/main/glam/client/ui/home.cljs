(ns glam.client.ui.home
  (:require [com.fulcrologic.fulcro.components :as c :refer [defsc]]
            [com.fulcrologic.fulcro.ui-state-machines :as sm]
            [glam.client.router :as r]
            [glam.client.ui.login :refer [Login ui-login]]
            [glam.client.ui.signup :refer [Signup ui-signup]]))

(defsc Home [this props]
  {:query         [{:home/signup (c/get-query Signup)}
                   {:home/login (c/get-query Login)}]
   :ident         (fn [_] [:component/id :home])
   :initial-state (fn [_] {:home/signup (c/get-initial-state Signup)
                           :home/login  (c/get-initial-state Login)})
   :route-segment (r/route-segment :home)}
  [:div.ui.grid
   [:div.eight.wide.column (ui-login (:home/login props))]
   [:div.eight.wide.column (ui-signup (:home/signup props))]])
