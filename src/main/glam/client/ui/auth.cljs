(ns glam.client.ui.auth
  (:require
    ["react" :as react]
    [com.fulcrologic.fulcro.algorithms.form-state :as fs]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom :as dom]
    [com.fulcrologic.fulcro.dom.html-entities :as ent]
    [com.fulcrologic.fulcro.mutations :as m]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [com.fulcrologic.fulcro.ui-state-machines :as sm]
    [sablono.core :as html :refer-macros [html]]
    [dv.fulcro-util :as fu]
    [glam.models.session :as session]
    [glam.client.router :as r]
    [taoensso.timbre :as log]))

(defsc Session
  "Session representation. Used primarily for server queries.
  On-screen representation happens in Login component."
  [_ {:keys [:session/valid? :user/email :session/server-error-msg]}]
  {:query         [:session/valid? :session/server-error-msg :user/email :ui/loading?]
   :ident         (fn [] [:component/id :session])
   :pre-merge     (fn [{:keys [data-tree]}]
                    (merge
                      {:session/valid?           false
                       :user/email               ""
                       :session/server-error-msg nil}
                      data-tree))
   :initial-state {:session/valid? false :user/email "" :session/server-error-msg nil}})

(def session-join {[:component/id :session] (comp/get-query Session)})

(defn get-session [props] (get props [:component/id :session]))
(defn valid-session? [props] (:session/valid? (get props [:component/id :session])))



